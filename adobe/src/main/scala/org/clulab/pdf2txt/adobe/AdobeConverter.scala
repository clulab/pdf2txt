package org.clulab.pdf2txt.scienceparse

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
import scala.io.Source

class AdobeConverter(credentialsFilename: String) extends PdfConverter {

  def this(credentialsFilenameOpt: Option[String]) =
      this(credentialsFilenameOpt.getOrElse(AdobeConverter.defaultCredentials))

  def this() = this(AdobeConverter.defaultCredentials)

  // Put the name of the file in the config
  val executionContextOpt =
      // Output a warning in this case.  Can only do local files.
      if (!new File(credentialsFilename).exists) None
      else {
        val credentials = Credentials
            .serviceAccountCredentialsBuilder()
            .fromFile(credentialsFilename)
            .build()

        Some(ExecutionContext.create(credentials))
      }
  val extractPdfOptions = ExtractPDFOptions.extractPdfOptionsBuilder()
      .addElementsToExtract(util.Arrays.asList(ExtractElementType.TEXT))
      .build()

  def convertElements(elements: Seq[AdobeElement]): String = {
    val stringBuffer = new StringBuffer()
    val ignore = ""

    elements.foreach { element =>
      // For now skip everything in a table.
      if (!element.inTable) {
        val text = element.text
        val extractedText = element.name match {
          case AdobeStage.Document => ignore
          case AdobeStage.Aside => text
          case AdobeStage.Figure => ignore
          case AdobeStage.Footnote => text

          case header if AdobeConverter.headers.contains(header) => text

          case AdobeStage.L => ignore
          case AdobeStage.Li => ignore
          case AdobeStage.Lbl => ignore
          case AdobeStage.Lbody => text

          case AdobeStage.P => text
          case AdobeStage.ParagraphSpan => text

          case AdobeStage.Reference => text
          case AdobeStage.Sect => ignore
          case AdobeStage.StyleSpan => ignore
          case AdobeStage.Sub => ignore

          case AdobeStage.Table => ignore
          case AdobeStage.TD => ignore
          case AdobeStage.TH => ignore
          case AdobeStage.TR => ignore

          case AdobeStage.Title => text
        }
        val dereferencedText = extractedText
        val separatedText = element.name match {
          case AdobeStage.ParagraphSpan => dereferencedText
          case _ =>
            if (dereferencedText.isEmpty) dereferencedText
            else dereferencedText + "\n\n"
        }

        stringBuffer.append(separatedText)
      }
    }
    stringBuffer.toString()
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

object AdobeConverter {
  val defaultCredentials = {
    val userHome = System.getProperty("user.home")
    s"$userHome/.pdf2txt/pdfservices-api-credentials.json"
  }
  val headers = Seq("H", "H1", "H2", "H3", "H4", "H5", "H6")
}
