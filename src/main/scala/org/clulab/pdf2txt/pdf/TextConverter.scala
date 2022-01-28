package org.clulab.pdf2txt.pdf

import java.io.InputStream
import scala.io.Source

class TextConverter extends PdfConverter {

  override def convert(inputStream: InputStream): String =
      Source.fromInputStream(inputStream).mkString
}
