package org.clulab.pdf2txt.google

import com.google.api.gax.core.CredentialsProvider
import com.google.auth.oauth2.GoogleCredentials
import com.google.auth.Credentials
import com.google.cloud.storage.Bucket
import com.google.cloud.storage.Storage.BlobListOption
import com.google.cloud.storage.StorageOptions
import com.google.cloud.vision.v1.{AsyncAnnotateFileRequest, Feature, GcsDestination, GcsSource, ImageAnnotatorClient, ImageAnnotatorSettings, InputConfig, OutputConfig}
import org.clulab.pdf2txt.common.pdf.PdfConverter
import org.clulab.pdf2txt.common.utils.Closer.AutoCloser
import org.clulab.pdf2txt.common.utils.FileEditor
import org.clulab.pdf2txt.common.utils.FileUtils
import org.json4s.{JArray, JInt, JString}
import org.json4s.jackson.JsonMethods

import java.io.{File, FileInputStream}
import java.nio.file.Files
import java.util.ArrayList
import java.util.concurrent.atomic.AtomicBoolean
import scala.beans.BeanProperty
import scala.collection.JavaConverters._

class GoogleConverter(googleSettings: GoogleSettings = GoogleConverter.defaultSettings) extends PdfConverter with CredentialsProvider {
  // See https://github.com/googleapis/java-vision/blob/HEAD/samples/snippets/src/main/java/com/example/vision/Detect.java.
  val isOpen = new AtomicBoolean(false)
  lazy val googleCredentials: GoogleCredentials = {
    val inputStream = new FileInputStream(new File(googleSettings.credentials))
    val googleCredentials = GoogleCredentials.fromStream(inputStream)

    googleCredentials
  }
  lazy val imageAnnotatorClient: ImageAnnotatorClient = {
    val imageAnnotatorSettings = ImageAnnotatorSettings.newBuilder().setCredentialsProvider(this).build()
    val imageAnnotatorClient = ImageAnnotatorClient.create(imageAnnotatorSettings)

    isOpen.set(true)
    imageAnnotatorClient
  }
  lazy val bucket: Bucket = {
    val storageOptions = StorageOptions.newBuilder().setCredentials(googleCredentials).build()
    val storage = storageOptions.getService
    val bucket = storage.get(googleSettings.bucket)

    bucket
  }

  def getCredentials: Credentials = googleCredentials

  override def close(): Unit = if (isOpen.getAndSet(false)) imageAnnotatorClient.close()

  def getText(jResponses: JArray): String = {
    val texts = jResponses.arr.map { response =>
      val text = (response \ "fullTextAnnotation" \ "text").asInstanceOf[JString].values

      text
    }

    texts.mkString("\n\f\n")
  }

  def convertPdf(pdfFile: File, jsonFile: File): String = {
    val pdfFileName = pdfFile.getName
    // Add a . to separate the prefix from the suffix.
    val jsonFilePrefix = FileEditor(pdfFile).setExt(inputExtension + ".").get.getName
    val bucketName = googleSettings.bucket
    val pdfBlob =
        if (Option(bucket.get(pdfFileName)).isDefined)
          throw new RuntimeException(s"""There is already a blob "$pdfFileName" in the "$bucketName" bucket.  Please remove it first.""")
        else
          bucket.create(pdfFileName, Files.readAllBytes(pdfFile.toPath))

    val gcsSourcePath = s"gs://$bucketName/$pdfFileName"
    val gcsDestinationPath = s"gs://$bucketName/$jsonFilePrefix"
    val gcsSource = GcsSource.newBuilder().setUri(gcsSourcePath).build()
    val inputConfig = InputConfig.newBuilder()
        .setMimeType("application/pdf")
        .setGcsSource(gcsSource)
        .build()
    val gcsDestination = GcsDestination.newBuilder().setUri(gcsDestinationPath).build()
    val outputConfig = OutputConfig.newBuilder()
        .setBatchSize(GoogleConverter.batchSize)
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
    val requests = {
      val requests = new ArrayList[AsyncAnnotateFileRequest]()

      requests.add(request)
      requests
    }

    // Wait for the request to finish. (The result is not used, since the
    // API saves the result to the specified location on GCS.)
    imageAnnotatorClient.asyncBatchAnnotateFilesAsync(requests).get().getResponsesList
    pdfBlob.delete()

    val pageList = bucket.list(BlobListOption.prefix(jsonFilePrefix))
    val pageAndResponseTuples = pageList.iterateAll.asScala.flatMap { jsonBlob =>
      val json = new String(jsonBlob.getContent()) // Doesn't this need a charset?
      val jValue = JsonMethods.parse(json)
      val jArray = (jValue \ "responses").asInstanceOf[JArray]
      val pageNumberAndResponsTuples = jArray.arr.map { response =>
        val pageNumber = (response \ "context" \ "pageNumber").asInstanceOf[JInt].values.toInt

        pageNumber -> response
      }

      jsonBlob.delete()
      pageNumberAndResponsTuples
    }.toVector
    val responses = pageAndResponseTuples.sortBy(_._1).map(_._2)
    val jResponses = JArray(responses.toList)
    val text = getText(jResponses)

    FileUtils.printWriterFromFile(jsonFile).autoClose { printWriter =>
      val json = JsonMethods.pretty(jResponses)

      printWriter.println(json)
    }
    text
  }

  def convertJson(jsonFile: File): String = {
    val json = FileUtils.getTextFromFile(jsonFile)
    val jResponses = JsonMethods.parse(json).asInstanceOf[JArray]
    val text = getText(jResponses)

    text
  }

  override def convert(pdfFile: File): String = {
    val jsonFile = new FileEditor(pdfFile).setExt(".json").get

    if (jsonFile.exists) convertJson(jsonFile)
    else convertPdf(pdfFile, jsonFile)
  }
}

case class GoogleSettings(@BeanProperty var credentials: String, @BeanProperty var bucket: String) {
  def this() = this("", "")
}

object GoogleConverter {
  val batchSize = 10
  val defaultCredentials: String = {
    val userHome = System.getProperty("user.home")
    s"$userHome/.pdf2txt/google-credentials.json"
  }
  val defaultBucket = "pdf2txt_pdfs"
  val defaultSettings: GoogleSettings = GoogleSettings(defaultCredentials, defaultBucket)
}
