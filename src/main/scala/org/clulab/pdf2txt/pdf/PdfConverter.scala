package org.clulab.pdf2txt.pdf

import java.io.InputStream

trait PdfConverter {
  def convert(inputStream: InputStream): String
}
