package org.clulab.pdf2txt.pdf

import org.clulab.pdf2txt.tika.Tika

import java.io.InputStream

class TikaConverter extends PdfConverter{
  protected val tika = new Tika()

  override def convert(inputStream: InputStream): String = tika.read(inputStream)
}
