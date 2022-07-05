package org.clulab.pdf2txt.adobe

import com.adobe.pdfservices.operation.ExecutionContext
import com.adobe.pdfservices.operation.auth.Credentials
import com.adobe.pdfservices.operation.io.FileRef
import com.adobe.pdfservices.operation.pdfops.ExtractPDFOperation
import com.adobe.pdfservices.operation.pdfops.options.extractpdf.{ExtractElementType, ExtractPDFOptions}
import net.lingala.zip4j.ZipFile
import org.clulab.pdf2txt.adobe.utils.{AdobeElement, AdobeStage}
import org.clulab.pdf2txt.common.pdf.PdfConverter
import org.clulab.pdf2txt.common.utils.Closer.AutoCloser
import org.clulab.pdf2txt.common.utils.FileEditor
import org.json4s.{JArray, JObject}
import org.json4s.jackson.JsonMethods

import java.io.File
import java.util
import scala.annotation.tailrec
import scala.beans.BeanProperty
import scala.io.Source

class AdobeConverter(adobeSettings: AdobeSettings = AdobeConverter.defaultSettings) extends PdfConverter {
  // Put the name of the file in the config
  val executionContextOpt =
      // Output a warning in this case.  Can only do local files.
      if (!new File(adobeSettings.credentials).exists) None
      else {
        val credentials = Credentials
            .serviceAccountCredentialsBuilder()
            .fromFile(adobeSettings.credentials)
            .build()

        Some(ExecutionContext.create(credentials))
      }
  val extractPdfOptions = ExtractPDFOptions.extractPdfOptionsBuilder()
      .addElementsToExtract(util.Arrays.asList(ExtractElementType.TEXT))
      .build()

  def convertElements(elements: Seq[AdobeElement]): String = {
    val stringBuffer = new StringBuffer()
    val ignore = ""
    val nonEmptyElements = elements.filter { element =>
      element.text.nonEmpty
    }
    val nonTableElements = nonEmptyElements.filterNot { element =>
      // For now skip everything in tables.  One might make an exception for
      // long paragraphs that are likely to contain full sentences.
      element.path.isIn(AdobeStage.Table) || element.path.isIn(AdobeStage.TOC)
    }

    nonTableElements.foreach { element =>
      val text = element.text
      val extractedText = element.name match {
        case AdobeStage.Document => ignore
        case AdobeStage.Aside => text
        case AdobeStage.Figure => ignore
        case AdobeStage.Footnote =>
          // Footnotes often begin with some numbers and maybe a space.
          if (text.head.isDigit)
            text.dropWhile(_.isDigit).dropWhile(_.isSpaceChar)
          // Sometimes it is a letter like a or b, especially for author affiliations.
          else if (text.head.isLower  && text.lift(1) == Some(' '))
            text.drop(2)
          else text

        case header if AdobeConverter.headers.contains(header) => text

        case AdobeStage.L => ignore
        case AdobeStage.LI => ignore
        case AdobeStage.Lbl => ignore
        case AdobeStage.LBody =>
          // These sometimes have the bullet still in front.
          if (text.head == '-' || text.head == '*') text.drop(1)
          else text

        case AdobeStage.P => text
        case AdobeStage.ParagraphSpan => text

        case AdobeStage.Reference =>
          // A reference within a reference can usually be ignored.
          if (element.path.isIn(AdobeStage.Reference)) ignore
          else text
        case AdobeStage.Sect => ignore
        case AdobeStage.Span => ignore
        case AdobeStage.ExtraCharSpan => ignore
        case AdobeStage.StyleSpan => ignore
        case AdobeStage.HyphenSpan => ignore
        case AdobeStage.NbspSpan => ignore
        case AdobeStage.Sub => ignore

        case AdobeStage.Table => ignore
        case AdobeStage.TD => ignore
        case AdobeStage.TH => ignore
        case AdobeStage.TR => ignore

        case AdobeStage.TOC => ignore
        case AdobeStage.TOCI => ignore

        case AdobeStage.Title => text
        case AdobeStage.Watermark => ignore
        case _ => text // Hope for maintainability.
      }
      val dereferencedText = dereference(extractedText)
      val separatedText = element.name match {
        case AdobeStage.ParagraphSpan if element.index == 1 =>
          // So far there have been at most ParagraphSpan[2]s so that only the
          // first ParagraphSpan needs not to be separated.
          dereferencedText
        case _ =>
          val trimmedText = dereferencedText.trim

          if (trimmedText.isEmpty) trimmedText
          else trimmedText + "\n\n"
      }

      stringBuffer.append(separatedText)
    }
    stringBuffer.toString()
  }

  def dereference(text: String): String = {
    val startText = "(<http"
    val endText = ">)"
    val allText = "(<>)"

    @tailrec
    def loopExternal(text: String): String = {
      // External links in a PDF include the URL.
      // This is coded conservatively in case the symbols are used for other reasons.
      val startPos = text.indexOf(startText)
      val endPos = text.indexOf(endText)

      if (0 <= startPos && startPos < endPos)
        loopExternal(text.substring(0, startPos) + text.substring(endPos + endText.length))
      else text
    }

    @tailrec
    def loopInternal(text: String): String = {
      val startPos = text.indexOf(allText)

      if (0 <= startPos)
        loopInternal(text.substring(0, startPos) + text.substring(startPos + allText.length))
      else text
    }

    loopInternal(loopExternal(text))
  }

  def convertJson(json: String): String = {
    val jValue = JsonMethods.parse(json)
    val jArray = (jValue \ "elements").asInstanceOf[JArray]
    val elements = jArray.arr.map { jValue =>
      AdobeElement(jValue.asInstanceOf[JObject])
    }

    convertElements(elements)
  }

  def convertZip(_zipFile: File): String = {
    val zipFile = new ZipFile(_zipFile)
    val fileHeader = zipFile.getFileHeader("structuredData.json")
    val inputStream = zipFile.getInputStream(fileHeader)
    val json = inputStream.autoClose { inputStream =>
      val source = Source.fromInputStream(inputStream)

      source.mkString
    }

    convertJson(json)
  }

  override def convert(pdfFile: File): String = {
    val zipFile = new FileEditor(pdfFile).setExt(".zip").get

    if (!zipFile.exists) {
      if (executionContextOpt.isEmpty)
        throw new RuntimeException("The AdobeConverter does not have the credentials to run.  It can only use previously converted documents.")
      val pdfFileRef = FileRef.createFromLocalFile(pdfFile.getAbsolutePath)
      val extractPdfOperation = {
        val extractPdfOperation = ExtractPDFOperation.createNew()

        extractPdfOperation.setOptions(extractPdfOptions)
        extractPdfOperation.setInputFile(pdfFileRef)
        extractPdfOperation
      }
      val zipFileRef = extractPdfOperation.execute(executionContextOpt.get)

      zipFileRef.saveAs(zipFile.getAbsolutePath)
    }
    convertZip(zipFile)
  }
}

case class AdobeSettings(@BeanProperty var credentials: String) {
  def this() = this("")
}

object AdobeConverter {
  val defaultCredentials = {
    val userHome = System.getProperty("user.home")
    s"$userHome/.pdf2txt/pdfservices-api-credentials.json"
  }
  val defaultSettings = AdobeSettings(defaultCredentials)
  val headers = Seq("H", "H1", "H2", "H3", "H4", "H5", "H6")
}
