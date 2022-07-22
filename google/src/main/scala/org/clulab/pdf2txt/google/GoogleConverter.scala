package org.clulab.pdf2txt.google

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport
import com.google.api.client.json.jackson2.JacksonFactory
import com.google.api.gax.core.CredentialsProvider
import com.google.auth.http.HttpCredentialsAdapter
import com.google.auth.oauth2.GoogleCredentials
import com.google.api.services.vision.v1.Vision
import com.google.api.services.vision.v1.VisionScopes
import com.google.auth.Credentials
import com.google.cloud.storage.Storage.BlobListOption
import com.google.cloud.storage.{Storage, StorageOptions}
import com.google.cloud.vision.v1.{AnnotateFileResponse, AsyncAnnotateFileRequest, Feature, GcsDestination, GcsSource, ImageAnnotatorClient, ImageAnnotatorSettings, InputConfig, OutputConfig}
import com.google.protobuf.util.JsonFormat
import org.clulab.pdf2txt.common.pdf.PdfConverter
import org.clulab.pdf2txt.common.utils.Closer.AutoCloser
import org.clulab.pdf2txt.common.utils.FileEditor

import java.io.{File, FileInputStream}
import java.util.ArrayList
import java.util.regex.Pattern
import scala.beans.BeanProperty
import scala.collection.JavaConverters._

class GoogleConverter(googleSettings: GoogleSettings = GoogleConverter.defaultSettings) extends PdfConverter {
  // See https://github.com/googleapis/java-vision/blob/HEAD/samples/snippets/src/main/java/com/example/vision/Detect.java.

  // make it a this
  class MyCredentialsProvider(googleCredentials: GoogleCredentials) extends CredentialsProvider {
    override def getCredentials: Credentials = googleCredentials
  }

  override def convert(pdfFile: File): String = {
    val inputStream = new FileInputStream(new File(googleSettings.credentials))
    val googleCredentials = GoogleCredentials.fromStream(inputStream).createScoped(VisionScopes.all())
    val credentialsProvider = new MyCredentialsProvider(googleCredentials)
    val imageAnnotatorSettings = ImageAnnotatorSettings.newBuilder().setCredentialsProvider(credentialsProvider).build()

    // TODO: Save the json files locally like the textract ones
    val client: ImageAnnotatorClient = ImageAnnotatorClient.create(imageAnnotatorSettings)
//    val storageOptions = StorageOptions.getDefaultInstance().getService()
    val storageOptions = StorageOptions.newBuilder().setCredentials(googleCredentials).build()
    val storage = storageOptions.getService()
    val stringBuilder = new StringBuilder()
    val maxPages = 10

    val pdfFileName = pdfFile.getName
    val jsonFilePrefix = FileEditor(pdfFile).setExt(inputExtension + ".").get.getName
    val bucket = "pdf2txt_pdfs"
    val gcsSourcePath = s"gs://$bucket/$pdfFileName"
    val gcsDestinationPath = s"gs://$bucket/$jsonFilePrefix"

    val gcsSource = GcsSource.newBuilder().setUri(gcsSourcePath).build()
    val inputConfig = InputConfig.newBuilder()
        .setMimeType("application/pdf")
        .setGcsSource(gcsSource)
        .build()
    val gcsDestination = GcsDestination.newBuilder().setUri(gcsDestinationPath).build()
    val outputConfig = OutputConfig.newBuilder()
        .setBatchSize(maxPages)
        .setGcsDestination(gcsDestination)
        .build()
    val feature = Feature.newBuilder()
        .setType(Feature.Type.DOCUMENT_TEXT_DETECTION)
        .build
    val request = AsyncAnnotateFileRequest.newBuilder()
        .addFeatures(feature)
        .setInputConfig(inputConfig)
        .setOutputConfig(outputConfig)
        .build()

    client.autoClose { client =>
      val requests = {
        val requests = new ArrayList[AsyncAnnotateFileRequest]()

        requests.add(request)
        requests
      }
      val response = client.asyncBatchAnnotateFilesAsync(requests)

      // Wait for the request to finish. (The result is not used, since the API saves the result to
      // the specified location on GCS.)
      response.get().getResponsesList()

      // Get the destination location from the gcsDestinationPath
      val pattern = Pattern.compile("gs://([^/]+)/(.+)")
      val matcher = pattern.matcher(gcsDestinationPath)

      // This can all be done ahead of time?
      if (matcher.find()) {
        val bucketName = matcher.group(1)
        val prefix = matcher.group(2)
        val bucket = storage.get(bucketName)
        val pageList = bucket.list(BlobListOption.prefix(prefix))

        pageList.iterateAll.asScala.foreach { blob =>
          println(blob.getName)

          val jsonContents = new String(blob.getContent()) // doesn't this need a charset?
          val builder = AnnotateFileResponse.newBuilder()
          JsonFormat.parser().merge(jsonContents, builder)

          val annotateFileResponse = builder.build()
          val annotateImageResponse = annotateFileResponse.getResponses(0) // get whole list instead?
          val text = annotateImageResponse.getFullTextAnnotation().getText()
          // each response has context/pageNumber

          // find the page number, make map of page number to json
          // combine them into an array after sorting
          // blob.delete()
          println(text)
          stringBuilder.append(text)
        }
      }
    }

    stringBuilder.toString
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
