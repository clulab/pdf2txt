package org.clulab.pdf2txt.scienceparse

import org.allenai.scienceparse.Parser
import org.clulab.pdf2txt.common.pdf.PdfConverter
import org.clulab.pdf2txt.common.utils.Closer.AutoCloser

import java.io.{BufferedInputStream, File, FileInputStream, InputStream}

class ScienceParseConverter() extends PdfConverter {
  val parser = Parser.getInstance()

  def read(inputStream: InputStream): String = {
    val extractedMetadata = parser.doParse(inputStream)

    extractedMetadata.toString
  }

  override def convert(file: File): String = {
    new BufferedInputStream(new FileInputStream(file)).autoClose { inputStream =>
      read(inputStream)
    }
  }
}
