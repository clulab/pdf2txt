package org.clulab.pdf2txt.document

import org.clulab.pdf2txt.common.utils.{Test, TextRange}
import org.clulab.pdf2txt.document.physical.DocumentByChar

class TestDocumentByChar extends Test {

  behavior of "DocumentByChar"

  it should "load files" in {
    val document = new DocumentByChar(None, TextRange("This is a test."))
  }
}
