package org.clulab.pdf2txt.common.utils

import org.clulab.pdf2txt.common.utils.Closer.AutoCloser

import java.io.File
import scala.io.{Codec, Source}

object FileUtils {

  protected def getTextFromSource(source: Source): String = source.mkString

  def getTextFromFile(file: File): String =
      Source.fromFile(file)(Codec.UTF8).autoClose { source =>
        getTextFromSource(source)
      }
}
