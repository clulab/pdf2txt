package org.clulab.pdf2txt.google

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport
import com.google.api.client.json.jackson2.JacksonFactory
import com.google.auth.http.HttpCredentialsAdapter
import com.google.auth.oauth2.GoogleCredentials
import com.google.api.services.vision.v1.Vision
import com.google.api.services.vision.v1.VisionScopes
import com.google.api.services.vision.v1.model.AnnotateFileRequest

import org.clulab.pdf2txt.common.pdf.PdfConverter
import org.clulab.pdf2txt.common.utils.Closer.AutoCloser
import org.clulab.pdf2txt.common.utils.FileEditor

import java.io.{File, FileInputStream}
import java.util
import scala.annotation.tailrec
import scala.beans.BeanProperty
import scala.io.Source

class GoogleConverter(googleSettings: GoogleSettings = GoogleConverter.defaultSettings) extends PdfConverter {
  // Put the name of the file in the config
  val visionOpt =
      // Output a warning in this case.  Can only do local files.
      if (!new File(googleSettings.credentials).exists) None
      else { // What should be in the file?
        val inputStream = new FileInputStream(new File(googleSettings.credentials))
        val credentials = GoogleCredentials.fromStream(inputStream).createScoped(VisionScopes.all())
        val jsonFactory = JacksonFactory.getDefaultInstance()
        val vision = new Vision.Builder(
          GoogleNetHttpTransport.newTrustedTransport(),
          jsonFactory,
          new HttpCredentialsAdapter(credentials)
        )

        vision
      }

  val request = new AnnotateFileRequest().
  val extractPdfOptions = ExtractPDFOptions.extractPdfOptionsBuilder()
      .addElementsToExtract(util.Arrays.asList(ExtractElementType.TEXT))
      .build()

  override def convert(pdfFile: File): String = {
    val bucket = "pdf2txt_pdfs"

    "empty"
  }
}

case class GoogleSettings(@BeanProperty var credentials: String) {
  def this() = this("")
}

object GoogleConverter {
  val defaultCredentials = {
    val userHome = System.getProperty("user.home")
    s"$userHome/.pdf2txt/google-credentials.json"
  }
  val defaultApplication = "pdf2txt"
  val defaultSettings = GoogleSettings(defaultCredentials)
}
