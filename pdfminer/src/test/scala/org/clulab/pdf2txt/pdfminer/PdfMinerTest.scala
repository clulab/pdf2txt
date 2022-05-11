package org.clulab.pdf2txt.pdfminer

import org.clulab.pdf2txt.common.utils.Test

import java.io.File

class PdfMinerTest extends Test {
  val pdfFilename = "./pdfminer/src/test/resources/org/clulab/pdf2txt/pdfminer/clu lab.pdf"
  lazy val pdfMiner = new PdfMinerConverter()

  behavior of "PdfMiner"

  ignore should "read a PDF file" in {
    assert(new File(pdfFilename).exists)

    val text = pdfMiner.convert(new File(pdfFilename))

    text should include ("The Computational Language Understanding")
    text should include ("please see our NLP")
  }
}
