package org.clulab.pdf2txt.ghostact

import org.clulab.pdf2txt.common.utils.Test

import java.io.File

class GhostActTest extends Test {
  // Access this as a file to test handling of the filename.
  val pdfFilename = "./ghostact/src/test/resources/org/clulab/pdf2txt/ghostact/clu lab.pdf"
  lazy val ghostAct = new GhostActConverter()

  behavior of "GhostAct"

  ignore should "read a PDF file" in {
    assert(new File(pdfFilename).exists)

    val text = ghostAct.convert(new File(pdfFilename))

    text should include ("The Computational Language Understanding")
    text should include ("please see our NLP")
  }
}
