package org.clulab.pdf2txt.scienceparse

import org.clulab.pdf2txt.common.utils.Closer.AutoCloser
import org.clulab.pdf2txt.common.utils.Test

import java.io.{File, InputStream}

class ScienceParseTest extends Test {
  val pdfFilename = "./scienceparse/src/test/resources/org/clulab/pdf2txt/scienceparse/clu lab.pdf"
  val pdfResourceName = "/org/clulab/pdf2txt/scienceparse/clu lab.pdf"

  def getInputStream(filename: String): InputStream = getClass.getResourceAsStream(filename)

  lazy val scienceParse = new ScienceParseConverter()

  behavior of "ScienceParse"

  it should "read a PDF stream" in {
    val text = getInputStream(pdfResourceName).autoClose { inputStream =>
      scienceParse.read(inputStream)
    }

    text should include ("The Computational Language Understanding")
    text should include ("please see our NLP")
  }

  ignore should "read a PDF file" in {
    assert(new File(pdfFilename).exists)

    val text = scienceParse.convert(new File(pdfFilename))

    text should include ("The Computational Language Understanding")
    text should include ("please see our NLP")
  }
}
