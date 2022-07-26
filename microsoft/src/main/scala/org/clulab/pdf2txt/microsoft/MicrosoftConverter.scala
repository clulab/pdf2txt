package org.clulab.pdf2txt.microsoft

import org.clulab.pdf2txt.common.pdf.PdfConverter
import org.clulab.pdf2txt.common.utils.Closer.AutoCloser

import java.io.{File, FileInputStream}
import java.util.concurrent.atomic.AtomicBoolean
import scala.annotation.tailrec
import scala.beans.BeanProperty
import scala.collection.JavaConverters._
import scala.collection.mutable

class MicrosoftConverter(microsoftSettings: MicrosoftSettings = MicrosoftConverter.defaultSettings) extends PdfConverter {

  override def close(): Unit = {
  }

  override def convert(pdfFile: File): String = {
    "hello"
  }
}

case class MicrosoftSettings(@BeanProperty var credentials: String, @BeanProperty var profile: String, @BeanProperty var region: String, @BeanProperty var bucket: String) {
  def this() = this("", "", "", "")
}

object MicrosoftConverter {
  val defaultCredentials: String = {
    val userHome = System.getProperty("user.home")
    s"$userHome/.pdf2txt/microsoft-credentials.properties"
  }
  val defaultProfile = "default"
  val defaultRegion = "us-west-1"
  val defaultBucket = ""
  val defaultSettings: MicrosoftSettings = MicrosoftSettings(defaultCredentials, defaultProfile, defaultRegion, defaultBucket)
}
