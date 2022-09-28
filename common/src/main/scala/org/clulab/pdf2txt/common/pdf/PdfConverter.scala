package org.clulab.pdf2txt.common.pdf

import org.clulab.pdf2txt.common.utils.MetadataHolder

import java.io.{Closeable, File}

trait PdfConverter extends Closeable {
  val  inputExtension: String = ".pdf"
  val outputExtension: String = ".txt"
  val   metaExtension: String = ".meta"

  def convert(file: File, metadataHolderOpt: Option[MetadataHolder] = None): String

  def close(): Unit = { }
}
