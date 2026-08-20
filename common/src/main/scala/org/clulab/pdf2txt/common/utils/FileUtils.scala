package org.clulab.pdf2txt.common.utils

import java.io.{File, FilenameFilter, PrintWriter}
import scala.io.{Codec, Source}
import scala.util.Using

object FileUtils {

  protected def getTextFromSource(source: Source): String = source.mkString

  def getTextFromFile(file: File): String = {
    Using.resource(Source.fromFile(file)(Codec.UTF8)) { source =>
      getTextFromSource(source)
    }
  }

  def getTextFromResource(resource: String): String = {
    Using.resource(Sourcer.sourceFromResource(resource)) { source =>
      getTextFromSource(source)
    }
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
    result.toSeq
  }
}
