package org.clulab.pdf2txt.microsoft

import org.clulab.pdf2txt.common.utils.Test
import org.scalatest.BeforeAndAfterAll

import java.io.File

class MicrosoftTest extends Test {
  val pdfFilename = "./microsoft/src/test/resources/org/clulab/pdf2txt/microsoft/clu lab.pdf"
  lazy val microsoft = new MicrosoftConverter()

  behavior of "Microsoft"

  ignore should "read a PDF file" in {
    assert(new File(pdfFilename).exists)
    if (new File(MicrosoftConverter.defaultCredentials).exists) {
      val text = microsoft.convert(new File(pdfFilename))

      text should include ("The Computational Language Understanding")
      text should include ("please see our NLP")
    }
  }
}
