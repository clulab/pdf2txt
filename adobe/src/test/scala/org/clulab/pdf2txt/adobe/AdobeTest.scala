package org.clulab.pdf2txt.adobe

import org.clulab.pdf2txt.common.utils.Test

import java.io.File

class AdobeTest extends Test {
  val pdfFilename = "./adobe/src/test/resources/org/clulab/pdf2txt/adobe/clu lab.pdf"

  lazy val adobe = new AdobeConverter()

  behavior of "Adobe"

  it should "read a PDF file" in {
    assert(new File(pdfFilename).exists)

    val text = adobe.convert(new File(pdfFilename))

    text should include ("The Computational Language Understanding")
    text should include ("please see our NLP")
  }
}
