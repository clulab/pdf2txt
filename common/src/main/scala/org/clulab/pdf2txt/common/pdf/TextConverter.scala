package org.clulab.pdf2txt.common.pdf

import java.io.File
import scala.io.{Codec, Source}

class TextConverter extends PdfConverter {

  override def convert(file: File): String =
      Source.fromFile(file)(Codec.UTF8).mkString
}
