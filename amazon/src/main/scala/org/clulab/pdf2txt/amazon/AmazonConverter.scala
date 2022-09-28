package org.clulab.pdf2txt.amazon

import org.clulab.pdf2txt.common.pdf.PdfConverter
import org.clulab.pdf2txt.common.utils.Closer.AutoCloser
import org.clulab.pdf2txt.common.utils.MetadataHolder
import software.amazon.awssdk.auth.credentials.ProfileCredentialsProvider
import software.amazon.awssdk.core.SdkBytes
import software.amazon.awssdk.profiles.ProfileFile
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.s3.S3Client
import software.amazon.awssdk.services.textract.TextractClient
import software.amazon.awssdk.services.textract.model.{Block, BlockType, DetectDocumentTextRequest, Document, DocumentLocation, GetDocumentTextDetectionRequest, GetDocumentTextDetectionResponse, S3Object, StartDocumentTextDetectionRequest}
import software.amazon.awssdk.services.s3.model.{DeleteObjectRequest, GetObjectAttributesRequest, NoSuchKeyException, ObjectAttributes, PutObjectRequest}

import java.io.{File, FileInputStream}
import java.util.concurrent.atomic.AtomicBoolean
import scala.annotation.tailrec
import scala.beans.BeanProperty
import scala.collection.JavaConverters._
import scala.collection.mutable

class AmazonConverter(amazonSettings: AmazonSettings = AmazonConverter.defaultSettings) extends PdfConverter {
  val s3IsOpen = new AtomicBoolean(false)
  val amazonIsOpen = new AtomicBoolean(false)
  val bucketName: String = amazonSettings.bucket
  lazy val credentialsProvider: ProfileCredentialsProvider = {
    val profileFile = ProfileFile.builder()
        .content(new File(amazonSettings.credentials).toPath)
        .`type`(ProfileFile.Type.CREDENTIALS)
        .build()

    ProfileCredentialsProvider.builder()
        .profileFile(profileFile)
        .profileName(amazonSettings.profile)
        .build()
  }
  lazy val s3Client: S3Client = {
    val s3Client = S3Client.builder()
        .credentialsProvider(credentialsProvider)
        .region(Region.of(amazonSettings.region))
        .build()

    s3IsOpen.set(true)
    s3Client
  }
  lazy val amazonClient: TextractClient = {
    val amazonClient = TextractClient.builder()
      .region(Region.of(amazonSettings.region))
      .credentialsProvider(credentialsProvider)
      .build()

    amazonIsOpen.set(true)
    amazonClient
  }

  override def close(): Unit = {
    if (s3IsOpen.getAndSet(false)) s3Client.close()
    if (amazonIsOpen.getAndSet(false)) amazonClient.close()
  }

  def s3FileExists(fileName: String): Boolean = {
    try {
      val getObjectAttributesRequest = GetObjectAttributesRequest.builder()
          .bucket(bucketName)
          .key(fileName)
          .objectAttributes(ObjectAttributes.OBJECT_SIZE)
          .build()
      val getObjectAttributesResponse = s3Client.getObjectAttributes(getObjectAttributesRequest)
      true
    }
    catch {
      case _: NoSuchKeyException => false
      case throwable: Throwable => throw throwable
    }
  }

  def s3UploadFile(file: File, fileName: String): Unit = {
    val putObjectRequest = PutObjectRequest.builder()
        .bucket(bucketName)
        .key(fileName)
        .build()
    val putObjectResponse = s3Client.putObject(putObjectRequest, file.toPath)
  }

  def s3DeleteFile(fileName: String): Unit = {
    val deleteObjectRequest = DeleteObjectRequest.builder()
      .bucket(bucketName)
      .key(fileName)
      .build()
    val deleteObjectResponse = s3Client.deleteObject(deleteObjectRequest)
  }

  def convertBlocks(blocks: mutable.Seq[Block]): String = {
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
          println("Unknown blocktype")
      }
    }
    stringBuilder.toString
  }

  def convertSinglePage(pdfFile: File): String = {
    val sdkBytes = new FileInputStream(pdfFile).autoClose { inputStream =>
      SdkBytes.fromInputStream(inputStream)
    }
    val document = Document.builder()
        .bytes(sdkBytes)
        .build()
    val detectDocumentTextRequest = DetectDocumentTextRequest.builder()
        .document(document)
        .build()
    val detectDocumentTextResponse = amazonClient.detectDocumentText(detectDocumentTextRequest)
    val blocks = detectDocumentTextResponse.blocks.asScala

    convertBlocks(blocks)
  }

  def convertMultiplePages(pdfFile: File): String = {
    val pdfFileName = pdfFile.getName

    def loopJobId(jobId: String): mutable.Seq[Block] = {

      def loopTokenOpt(tokenOpt: Option[String]): GetDocumentTextDetectionResponse = {
        val getDocumentTextDetectionRequest = {
          val builder = GetDocumentTextDetectionRequest.builder()
            .jobId(jobId)

          tokenOpt.foreach(builder.nextToken)
          builder.build()
        }

        @tailrec
        def loopRequest(): GetDocumentTextDetectionResponse = {
          val getDocumentTextDetectionResponse = amazonClient.getDocumentTextDetection(getDocumentTextDetectionRequest)

          if (getDocumentTextDetectionResponse.jobStatusAsString != "IN_PROGRESS")
            getDocumentTextDetectionResponse
          else {
            Thread.sleep(1000)
            loopRequest()
          }
        }

        loopRequest()
      }

      def loopBlocks(): mutable.Seq[Block] = {
        val blocks = mutable.ArrayBuffer[Block]()

        @tailrec
        def loopNextTokenOpt(tokenOpt: Option[String]): Unit = {
          val getDocumentTextDetectionResponse = loopTokenOpt(tokenOpt)
          val nextTokenOpt = Option(getDocumentTextDetectionResponse.nextToken())

          blocks ++= getDocumentTextDetectionResponse.blocks.asScala
          if (nextTokenOpt.isDefined)
            loopNextTokenOpt(nextTokenOpt)
        }

        loopNextTokenOpt(None)
        blocks
      }

      loopBlocks()
    }

    if (s3FileExists(pdfFileName))
      throw new RuntimeException(s"""There is already a blob "$pdfFileName" in the "$bucketName" bucket.  Please remove it first.""")
    else
      s3UploadFile(pdfFile, pdfFileName)

    val s3Object = S3Object.builder()
        .bucket(bucketName)
        .name(pdfFileName)
        .build()
    val documentLocation = DocumentLocation.builder()
        .s3Object(s3Object)
        .build()
    val startDocumentTextDetectionRequest = StartDocumentTextDetectionRequest.builder()
        .documentLocation(documentLocation)
        .build()
    val startDocumentTextDetectionRequestResponse = amazonClient.startDocumentTextDetection(startDocumentTextDetectionRequest)
    val jobId = startDocumentTextDetectionRequestResponse.jobId()
    val blocks = loopJobId(jobId)
    val result = convertBlocks(blocks)

    s3DeleteFile(pdfFileName)
    result
  }

  override def convert(pdfFile: File, metadataHolderOpt: Option[MetadataHolder] = None): String = {
    if (amazonSettings.bucket.isEmpty) convertSinglePage(pdfFile)
    else convertMultiplePages(pdfFile)
  }
}

case class AmazonSettings(@BeanProperty var credentials: String, @BeanProperty var profile: String, @BeanProperty var region: String, @BeanProperty var bucket: String) {
  def this() = this("", "", "", "")
}

object AmazonConverter {
  val defaultCredentials: String = {
    val userHome = System.getProperty("user.home")
    s"$userHome/.pdf2txt/aws-credentials.properties"
  }
  val defaultProfile = "default"
  val defaultRegion = "us-west-1"
  val defaultBucket = ""
  val defaultSettings: AmazonSettings = AmazonSettings(defaultCredentials, defaultProfile, defaultRegion, defaultBucket)
}
