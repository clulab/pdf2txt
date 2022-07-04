package org.clulab.pdf2txt.textract

import org.clulab.pdf2txt.common.pdf.PdfConverter
import org.clulab.pdf2txt.common.utils.Closer.AutoCloser
import software.amazon.awssdk.auth.credentials.ProfileCredentialsProvider
import software.amazon.awssdk.core.SdkBytes
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.textract.TextractClient
import software.amazon.awssdk.services.textract.model.{DetectDocumentTextRequest, Document}

import java.io.{File, FileInputStream}

import scala.collection.JavaConverters._

class TextractConverter(region: Region, profile: String, credentialsFilename: String) extends PdfConverter {
  val textractClient = {
    // Does this return null?
    val credentialsProvider = ProfileCredentialsProvider.create(profile)

    TextractClient.builder
      .region(region)
      .credentialsProvider(credentialsProvider)
      .build()
  }

  override def close(): Unit = textractClient.close()

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
    val strings = blocks.map { block =>
      val blockType = block.blockType // skip if it is a page? or only do if page?
      // separate pages with FF?
      val text = block.text()
      text
    }
    val text = strings.mkString("", "\n", "\n")

    text
  }
}

object TextractConverter {
  val defaultCredentials = {
    val userHome = System.getProperty("user.home")
    s"$userHome/.aws/credentials"
  }
  val defaultProfile = "textract"
}
