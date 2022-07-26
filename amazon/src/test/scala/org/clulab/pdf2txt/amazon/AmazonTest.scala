package org.clulab.pdf2txt.amazon

import org.clulab.pdf2txt.common.utils.Test
import org.scalatest.BeforeAndAfterAll

import java.io.File

class AmazonTest extends Test with BeforeAndAfterAll {
  val pdfFilename = "./amazon/src/test/resources/org/clulab/pdf2txt/amazon/clu lab.pdf"
  lazy val amazon = new AmazonConverter()

  override def afterAll(): Unit = amazon.close()

  behavior of "Amazon"

  ignore should "read a PDF file" in {
    assert(new File(pdfFilename).exists)
    if (new File(AmazonConverter.defaultCredentials).exists) {
      val text = amazon.convert(new File(pdfFilename))

      text should include ("The Computational Language Understanding")
      text should include ("please see our NLP")
    }
  }
}
