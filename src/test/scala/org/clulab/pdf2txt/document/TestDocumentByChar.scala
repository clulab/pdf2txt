package org.clulab.pdf2txt.document

import org.clulab.pdf2txt.common.utils.Test
import org.clulab.pdf2txt.document.physical.DocumentByChar

class TestDocumentByChar extends Test {

  behavior of "DocumentByChar"

  it should "load files" in {
    val document = DocumentByChar("This is a test.")
  }
}
