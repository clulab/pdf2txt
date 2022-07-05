package org.clulab.pdf2txt.textract

import org.clulab.pdf2txt.common.utils.Test
import org.scalatest.BeforeAndAfterAll

import java.io.File

class TextractTest extends Test with BeforeAndAfterAll {
  val pdfFilename = "./textract/src/test/resources/org/clulab/pdf2txt/textract/clu lab.pdf"

  lazy val textract = new TextractConverter()

  override def afterAll(): Unit = textract.close()

  behavior of "Textract"

  ignore should "read a PDF file" in {
    assert(new File(pdfFilename).exists)
    if (new File(TextractConverter.defaultCredentials).exists) {
      val text = textract.convert(new File(pdfFilename))

      text should include ("The Computational Language Understanding")
      text should include ("please see our NLP")
    }
  }
}
