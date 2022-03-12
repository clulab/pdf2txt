package org.clulab.pdf2txt.common.pdf

import java.io.File

trait PdfConverter {
  def convert(file: File): String
  def close(): Unit = { }
}
