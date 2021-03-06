package org.clulab.pdf2txt.common.pdf

import java.io.{Closeable, File}

trait PdfConverter extends Closeable {
  val  inputExtension: String = ".pdf"
  val outputExtension: String = ".txt"

  def convert(file: File): String
  def close(): Unit = { }
}
