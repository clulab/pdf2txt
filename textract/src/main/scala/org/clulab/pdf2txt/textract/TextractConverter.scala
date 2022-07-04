package org.clulab.pdf2txt.textract

import org.clulab.pdf2txt.common.pdf.PdfConverter
import org.clulab.pdf2txt.common.utils.Closer.AutoCloser
import software.amazon.awssdk.auth.credentials.ProfileCredentialsProvider
import software.amazon.awssdk.core.SdkBytes
import software.amazon.awssdk.profiles.ProfileFile
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.textract.TextractClient
import software.amazon.awssdk.services.textract.model.{BlockType, DetectDocumentTextRequest, Document}

import java.io.{File, FileInputStream}
import java.util.concurrent.atomic.AtomicBoolean
import scala.beans.BeanProperty
import scala.collection.JavaConverters._

class TextractConverter(textractSettings: TextractSettings = TextractConverter.defaultSettings) extends PdfConverter {
  val isOpen = new AtomicBoolean(false)
  lazy val textractClient = {
    val credentialsProvider = {
      val profileFile = ProfileFile.builder()
          .content(new File(textractSettings.credentials).toPath)
          .`type`(ProfileFile.Type.CREDENTIALS)
          .build()

      ProfileCredentialsProvider.builder()
          .profileFile(profileFile)
          .profileName(textractSettings.profile)
          .build()
    }

    val textractClient = TextractClient.builder
        .region(Region.of(textractSettings.region))
        .credentialsProvider(credentialsProvider)
        .build()

    isOpen.set(true)
    textractClient
  }

  override def close(): Unit = if (isOpen.getAndSet(false)) textractClient.close()

  override def convert(pdfFile: File): String = {
    val sdkBytes = new FileInputStream(pdfFile).autoClose { inputStream =>
      SdkBytes.fromInputStream(inputStream)
    }
    val document = Document.builder()
        .bytes(sdkBytes)
        .build()
    val detectDocumentTextRequest = DetectDocumentTextRequest.builder()
        .document(document)
        .build()
    val detectDocumentTextResponse = textractClient.detectDocumentText(detectDocumentTextRequest)
    val blocks = detectDocumentTextResponse.blocks.asScala
    // It doesn't need to be thread-safe, so the faster StringBuilder is preferred.
    val stringBuilder = new StringBuilder()

    blocks.foreach { block =>
      val blockType = block.blockType

      blockType match {
        case BlockType.PAGE =>
          if (stringBuilder.nonEmpty) stringBuilder.append("\f")
        case BlockType.LINE =>
          stringBuilder.append(block.text)
          stringBuilder.append("\n")
        case BlockType.WORD =>
          // These appear to be duplicated in the lines.
        case _ =>
      }
    }
    stringBuilder.toString
  }
}

case class TextractSettings(@BeanProperty var credentials: String, @BeanProperty var profile: String, @BeanProperty var region: String) {
  def this() = this("", "", "")
}

object TextractConverter {
  val defaultCredentials = {
    val userHome = System.getProperty("user.home")
    s"$userHome/.pdf2txt/aws-credentials.properties"
  }
  val defaultProfile = "default"
  val defaultRegion = "us-west-1"
  val defaultSettings = TextractSettings(defaultCredentials, defaultProfile, defaultRegion)
}
