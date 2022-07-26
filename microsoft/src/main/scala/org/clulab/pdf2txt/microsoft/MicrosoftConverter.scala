package org.clulab.pdf2txt.microsoft

import com.microsoft.azure.cognitiveservices.vision.computervision.implementation.ComputerVisionImpl
import com.microsoft.azure.cognitiveservices.vision.computervision.models.ReadInStreamOptionalParameter
import com.microsoft.azure.cognitiveservices.vision.computervision.{ComputerVisionClient, ComputerVisionManager}
import org.clulab.pdf2txt.common.pdf.PdfConverter
import org.clulab.pdf2txt.common.utils.Closer.AutoCloser
import rx.Observable

import java.io.{BufferedInputStream, File, FileInputStream}
import java.nio.file.Files
import java.util.Properties
import scala.beans.BeanProperty

class MicrosoftConverter(microsoftSettings: MicrosoftSettings = MicrosoftConverter.defaultSettings) extends PdfConverter {

  def getKey: String = {
    val properties = new Properties()

    new BufferedInputStream(new FileInputStream(new File(microsoftSettings.credentials))).autoClose { bufferedInputStream =>
      properties.load(bufferedInputStream)
    }
    properties.getProperty("key")
  }

  val computerVisionClient: ComputerVisionClient = ComputerVisionManager.authenticate(getKey).withEndpoint(microsoftSettings.endpoint)


  override def close(): Unit = ()

  override def convert(pdfFile: File): String = {
    val bytes = Files.readAllBytes(pdfFile.toPath)
    // Put this higher up
    val computerVision = computerVisionClient.computerVision.asInstanceOf[ComputerVisionImpl]
    val readInStreamHeaders = computerVision
        .readInStreamWithServiceResponseAsync(bytes, null)
        .toBlocking()
        .single()
        .headers()
    val operationLocation = readInStreamHeaders.operationLocation()


    // need to get location

    operationLocation
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
