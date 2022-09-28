package org.clulab.pdf2txt.microsoft

import com.microsoft.azure.cognitiveservices.vision.computervision.implementation.ComputerVisionImpl
import com.microsoft.azure.cognitiveservices.vision.computervision.models.{AnalyzeResults, OperationStatusCodes}
import com.microsoft.azure.cognitiveservices.vision.computervision.ComputerVisionManager
import org.clulab.pdf2txt.common.pdf.PdfConverter
import org.clulab.pdf2txt.common.utils.Closer.AutoCloser
import org.clulab.pdf2txt.common.utils.MetadataHolder

import java.io.{BufferedInputStream, File, FileInputStream}
import java.nio.file.Files
import java.util.{Properties, UUID}
import scala.annotation.tailrec
import scala.beans.BeanProperty
import scala.collection.JavaConverters._

// See https://docs.microsoft.com/en-us/azure/cognitive-services/computer-vision/quickstarts-sdk/client-library?tabs=visual-studio&pivots=programming-language-java.
class MicrosoftConverter(microsoftSettings: MicrosoftSettings = MicrosoftConverter.defaultSettings) extends PdfConverter {

  def getKey: String = {
    val properties = new Properties()

    new BufferedInputStream(new FileInputStream(new File(microsoftSettings.credentials))).autoClose { bufferedInputStream =>
      properties.load(bufferedInputStream)
    }
    properties.getProperty("key")
  }

  val computerVision: ComputerVisionImpl = ComputerVisionManager
      .authenticate(getKey)
      .withEndpoint(microsoftSettings.endpoint)
      .computerVision
      .asInstanceOf[ComputerVisionImpl]

  override def close(): Unit = ()

  def getAnalyzeResults(operationUUID: UUID): AnalyzeResults = {

    @tailrec
    def loop(): AnalyzeResults = {
      Thread.sleep(1000)

      val readOperationResultOpt = Option(computerVision.getReadResult(operationUUID))
      val statusOpt = readOperationResultOpt.map(_.status)
      val succeeded = statusOpt.exists { status =>
        if (status == OperationStatusCodes.FAILED)
          throw new RuntimeException("The service reports that the conversion failed.")
        status == OperationStatusCodes.SUCCEEDED
      }

      if (succeeded) readOperationResultOpt.get.analyzeResult()
      else loop()
    }

    loop()
  }

  override def convert(pdfFile: File, metadataHolderOpt: Option[MetadataHolder] = None): String = {
    val bytes = Files.readAllBytes(pdfFile.toPath)
    val readInStreamHeaders = computerVision
        .readInStreamWithServiceResponseAsync(bytes, null)
        .toBlocking
        .single()
        .headers()
    val operationId = readInStreamHeaders
        .operationLocation()
        .split('/')
        .last
    val operationUUID = UUID.fromString(operationId)
    val analyzeResults = getAnalyzeResults(operationUUID)
    val sortedReadResults = analyzeResults.readResults.asScala.sortBy { readResult => readResult.page }
    val text = sortedReadResults.map { readResult =>
      readResult.lines.asScala.map(_.text()).mkString("\n")
    }.mkString("\n\f\n")

    text
  }
}

case class MicrosoftSettings(@BeanProperty var credentials: String, @BeanProperty var endpoint: String) {
  def this() = this("", "")
}

object MicrosoftConverter {
  val defaultCredentials: String = {
    val userHome = System.getProperty("user.home")
    s"$userHome/.pdf2txt/microsoft-credentials.properties"
  }
  val defaultEndpoint = ""
  val defaultSettings: MicrosoftSettings = MicrosoftSettings(defaultCredentials, defaultEndpoint)
}
