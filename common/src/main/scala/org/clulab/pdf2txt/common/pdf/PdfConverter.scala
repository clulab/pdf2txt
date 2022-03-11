package org.clulab.pdf2txt.common.pdf

import java.io.{File, InputStream}

trait PdfConverter {
  def convert(file: File): String
}
