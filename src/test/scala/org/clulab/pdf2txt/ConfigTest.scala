package org.clulab.pdf2txt

import org.clulab.pdf2txt.common.pdf.TextConverter
import org.clulab.pdf2txt.common.utils.Test

class ConfigTest extends Test {

  behavior of "config"

  it should "be accessible" in {
    val pdf2txt = new Pdf2txt(new TextConverter(), Array.empty)
    val message = pdf2txt.getArgString("message", None)

    println(message)
  }
}
