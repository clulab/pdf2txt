package org.clulab.pdf2txt

import org.clulab.pdf2txt.common.utils.BuildUtils
import org.clulab.pdf2txt.common.utils.Sourcer
import org.clulab.pdf2txt.common.utils.Test

class TestResource extends Test {

  def getTextFromResource(path: String): String = {
    val source = Sourcer.sourceFromResource(path)
    val text = source.mkString

    source.close
    text
  }

  behavior of "resource"

  it should "be accessible" in {
    // package;format="packaged" results in backlashes and
    // syntax errors on Windows, so this is converted manually.
    println(getTextFromResource("/" + BuildUtils.pkgToDir("org.clulab.pdf2txt") + "/resource.txt"))
  }
}
