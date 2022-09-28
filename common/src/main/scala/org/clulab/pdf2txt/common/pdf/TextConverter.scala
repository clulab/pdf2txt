package org.clulab.pdf2txt.common.pdf

import org.clulab.pdf2txt.common.utils.{FileUtils, MetadataHolder}

import java.io.File

class TextConverter() extends PdfConverter() {
  override val inputExtension: String = ".txt"

  override def convert(file: File, metadataHolderOpt: Option[MetadataHolder] = None): String = FileUtils.getTextFromFile(file)
}
