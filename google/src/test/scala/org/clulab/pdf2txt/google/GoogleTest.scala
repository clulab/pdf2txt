package org.clulab.pdf2txt.google

import org.clulab.pdf2txt.common.utils.Test
import org.scalatest.BeforeAndAfterAll

import java.io.File

class GoogleTest extends Test with BeforeAndAfterAll {
  val pdfFilename = "./google/src/test/resources/org/clulab/pdf2txt/google/clu lab.pdf"

  lazy val google = new GoogleConverter()

  override def afterAll(): Unit = google.close()

  behavior of "Google"

  ignore should "read a PDF file" in {
    assert(new File(pdfFilename).exists)

    if (new File(GoogleConverter.defaultCredentials).exists) {
      val text = google.convert(new File(pdfFilename))

      text should include ("The Computational Language Understanding")
      text should include ("please see our NLP")
    }
  }
}
