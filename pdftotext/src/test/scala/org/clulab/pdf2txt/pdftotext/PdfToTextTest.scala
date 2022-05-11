package org.clulab.pdf2txt.pdftotext

import org.clulab.pdf2txt.common.utils.Test

import java.io.File

class PdfToTextTest extends Test {
  // Access this as a file to test handling of the filename.
  val pdfFilename = "./pdftotext/src/test/resources/org/clulab/pdf2txt/pdftotext/clu lab.pdf"
  lazy val pdfToText = new PdfToTextConverter()

  behavior of "PdfToText"

  ignore should "read a PDF file" in {
    assert(new File(pdfFilename).exists)

    val text = pdfToText.convert(new File(pdfFilename))

    text should include ("The Computational Language Understanding")
    text should include ("please see our NLP")
  }
}
