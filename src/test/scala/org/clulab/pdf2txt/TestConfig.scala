package org.clulab.pdf2txt

import org.clulab.pdf2txt.common.utils.Test

class TestConfig extends Test {

  behavior of "config"

  it should "be accessible" in {
    val pdf2txt = Pdf2txt()
    val message = pdf2txt.getArgString("Pdf2txt.message", None)

    println(message)
  }
}
