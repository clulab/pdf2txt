package org.clulab.pdf2txt.textract

import org.clulab.pdf2txt.common.utils.Test
import software.amazon.awssdk.regions.Region

import java.io.File

class TextractTest extends Test {
  val pdfFilename = "./textract/src/test/resources/org/clulab/pdf2txt/textract/clu lab.pdf"

  // Get region from somewhere.
  lazy val textract = new TextractConverter(Region.US_WEST_1, TextractConverter.defaultProfile, TextractConverter.defaultCredentials)

  behavior of "Textract"

  it should "read a PDF file" in {
    assert(new File(pdfFilename).exists)

    if (new File(TextractConverter.defaultCredentials).exists) {
      val text = textract.convert(new File(pdfFilename))

      text should include ("The Computational Language Understanding")
      text should include ("please see our NLP")
    }
  }
}
