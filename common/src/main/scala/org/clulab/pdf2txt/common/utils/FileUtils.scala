package org.clulab.pdf2txt.common.utils

import org.clulab.pdf2txt.common.utils.Closer.AutoCloser

import java.io.{File, FilenameFilter, PrintWriter}
import scala.io.{Codec, Source}

object FileUtils {

  protected def getTextFromSource(source: Source): String = source.mkString

  def getTextFromFile(file: File): String = {
    val source = Source.fromFile(file)(Codec.UTF8)

    source.autoClose(getTextFromSource)
  }

  def getTextFromResource(resource: String): String = {
    val source = Sourcer.sourceFromResource(resource)

    source.autoClose(getTextFromSource)
  }

  def printWriterFromFile(file: File): PrintWriter = {
    Sinker.printWriterFromFile(file, append = false)
  }

  def findFiles(collectionDir: String, extension: String): Seq[File] = {
    val dir = new File(collectionDir)
    val filter = new FilenameFilter {
      def accept(dir: File, name: String): Boolean = name.endsWith(extension)
    }

    val result = Option(dir.listFiles(filter))
      .getOrElse(throw Sourcer.newFileNotFoundException(collectionDir))
    result
  }
}
