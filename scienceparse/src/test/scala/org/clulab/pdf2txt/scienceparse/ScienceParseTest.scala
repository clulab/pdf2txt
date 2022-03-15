package org.clulab.pdf2txt.scienceparse

import org.clulab.pdf2txt.common.utils.Closer.AutoCloser
import org.clulab.pdf2txt.common.utils.Test

import java.io.InputStream

class ScienceParseTest extends Test {
  val pdfFilename = "/org/clulab/pdf2txt/scienceparse/clulab.pdf"

  def getInputStream(filename: String): InputStream = getClass.getResourceAsStream(filename)

  val scienceParse = new ScienceParseConverter()

  behavior of "ScienceParse"

  it should "read a PDF file" in {
    val text = getInputStream(pdfFilename).autoClose { inputStream =>
      scienceParse.read(inputStream)
    }

    text should include ("The Computational Language Understanding")
    text should include ("please see our NLP")
  }
}
