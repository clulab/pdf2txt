package org.clulab.pdf2txt

import me.shadaj.scalapy.py
import me.shadaj.scalapy.py.SeqConverters
import org.clulab.pdf2txt.common.utils.Test

class PythonTest extends Test {

  behavior of "Python"

  it should "be accessible" in {
    val listLengthPython = py.Dynamic.global.len(List(1, 2, 3).toPythonProxy)
    val listLength = listLengthPython.as[Int]

    listLength should be (3)
  }
}
