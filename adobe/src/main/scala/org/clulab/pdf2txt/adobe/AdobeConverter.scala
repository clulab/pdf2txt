package org.clulab.pdf2txt.adobe

import com.adobe.pdfservices.operation.{PDFServices, PDFServicesMediaType}
import com.adobe.pdfservices.operation.auth.ServicePrincipalCredentials
import com.adobe.pdfservices.operation.pdfjobs.jobs.ExtractPDFJob
import com.adobe.pdfservices.operation.pdfjobs.params.extractpdf.{ExtractElementType, ExtractPDFParams}
import com.adobe.pdfservices.operation.pdfjobs.result.ExtractPDFResult
import net.lingala.zip4j.ZipFile
import org.apache.commons.io.IOUtils
import org.clulab.pdf2txt.adobe.utils.{AdobeElement, AdobeStage}
import org.clulab.pdf2txt.common.pdf.PdfConverter
import org.clulab.pdf2txt.common.utils.{FileEditor, MetadataHolder}
import org.json4s.{JArray, JObject}
import org.json4s.jackson.JsonMethods
import org.json4s.jvalue2monadic

import java.io.{BufferedInputStream, File, FileInputStream}
import java.nio.file.Files
import java.util
import java.util.Properties
import scala.annotation.tailrec
import scala.beans.BeanProperty
import scala.io.Source
import scala.util.Using

class AdobeConverter(adobeSettings: AdobeSettings = AdobeConverter.defaultSettings) extends PdfConverter {
  // Put the name of the file in the config
  val pdfServicesOpt: Option[PDFServices] =
      // Output a warning in this case.  Can only do local files.
      if (!new File(adobeSettings.credentials).exists) None
      else {
        val credentials = Using.resource(new BufferedInputStream(new FileInputStream(new File(adobeSettings.credentials)))) { bufferedInputStream =>
          val properties = new Properties()

          properties.load(bufferedInputStream)
          new ServicePrincipalCredentials(
            properties.getProperty("PDF_SERVICES_CLIENT_ID"),
            properties.getProperty("PDF_SERVICES_CLIENT_SECRET")
          )
        }
        Some(new PDFServices(credentials))
      }
  val extractPdfParams: ExtractPDFParams = ExtractPDFParams.extractPDFParamsBuilder()
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
    var prevNameOpt = Option.empty[String]

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
          else if (text.head.isLower  && text.lift(1).contains(' '))
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
        case AdobeStage.Sub =>
          text

        case AdobeStage.Table => ignore
        case AdobeStage.TD => ignore
        case AdobeStage.TH => ignore
        case AdobeStage.TR => ignore

        case AdobeStage.TOC => ignore
        case AdobeStage.TOCI => ignore

        case AdobeStage.Title => text
        case AdobeStage.TOC => text
        case AdobeStage.TOCI => text
        case AdobeStage.Watermark => ignore
        case _ => text // Hope for maintainability.
      }
      val dereferencedText = dereference(extractedText)
      val separatedText = element.name match {
        case AdobeStage.ParagraphSpan if element.index == 1 =>
          // So far there have been at most ParagraphSpan[2]s so that only the
          // first ParagraphSpan needs not to be separated.
          dereferencedText
        case AdobeStage.Sub =>
          val trimmedText = dereferencedText.trim

          if (trimmedText.isEmpty) trimmedText
          else trimmedText + "\n"
        case _ =>
          val trimmedText = dereferencedText.trim

          if (trimmedText.isEmpty) trimmedText
          else trimmedText + "\n\n"
      }
      // Only insert an extra newline when transitioning out of a Sub
      if (prevNameOpt.contains(AdobeStage.Sub) && element.name != AdobeStage.Sub)
        stringBuffer.append("\n")
      stringBuffer.append(separatedText)
      prevNameOpt = Some(element.name)
    }
    stringBuffer.toString
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
    val json = Using.resource(zipFile.getInputStream(fileHeader)) { inputStream =>
      val source = Source.fromInputStream(inputStream)

      source.mkString
    }

    convertJson(json)
  }

  def convertPdf(pdfFile: File, zipFile: File): Unit = {
    if (pdfServicesOpt.isEmpty)
      throw new RuntimeException("The AdobeConverter does not have the credentials to run.  It can only use previously converted documents.")
    val pdfServices = pdfServicesOpt.get
    val asset = Using.resource(Files.newInputStream(pdfFile.toPath)) { inputStream =>
      pdfServices.upload(inputStream, PDFServicesMediaType.PDF.getMediaType)
    }
    val extractPDFJob = new ExtractPDFJob(asset).setParams(extractPdfParams)
    val location = pdfServices.submit(extractPDFJob)
    val pdfServicesResponse = pdfServices.getJobResult(location, classOf[ExtractPDFResult])
    val resultAsset = pdfServicesResponse.getResult.getResource
    val streamAsset = pdfServices.getContent(resultAsset)

    Using.resource(Files.newOutputStream(zipFile.toPath)) { outputStream =>
      IOUtils.copy(streamAsset.getInputStream, outputStream);
    }
  }

  override def convert(pdfFile: File, metadataHolderOpt: Option[MetadataHolder] = None): String = {
    val zipFile = new FileEditor(pdfFile).setExt(".zip").get

    if (!zipFile.exists)
      convertPdf(pdfFile, zipFile)
    convertZip(zipFile)
  }
}

case class AdobeSettings(@BeanProperty var credentials: String) {
  def this() = this("")
}

object AdobeConverter {
  val defaultCredentials: String = {
    val userHome = System.getProperty("user.home")
    s"$userHome/.pdf2txt/pdfservices-api-credentials.properties"
  }
  val defaultSettings: AdobeSettings = AdobeSettings(defaultCredentials)
  val headers: Seq[String] = Seq("H", "H1", "H2", "H3", "H4", "H5", "H6")
}
