package org.clulab.pdf2txt.preprocessor

import org.clulab.pdf2txt.common.utils.Test

class TestLineBreakPreprocessor extends Test {
  val preprocessor = new LineBreakPreprocessor()

  behavior of "LineBreakPreprocessor"

  def test(inputText: String, expectedOutputText: String): Unit = {
    it should s"convert ${escape(inputText)}" in {
      val actualOutputText = preprocessor.preprocess(inputText).toString

      actualOutputText shouldBe expectedOutputText
    }
  }

  test("one two three", "one two three")
  test("- two three", "- two three")
  test("- two -", "- two -")

  test("one\ntwo\nthree", "one\ntwo\nthree")
  test("-\ntwo\nthree", "-\ntwo\nthree")
  test("-\ntwo\n-", "-\ntwo\n-")

  test("-one\ntwo\nthree", "-one\ntwo\nthree")
  test("one-\ntwo\nthree", "onetwo\nthree")
  test("one-\ntwo\n-", "onetwo\n-")

  test("one-\ntwo-\nthree", "onetwothree")

  test("A pre-\nhensile tail is un-\ncommon.", "A prehensile tail is uncommon.")
  test("A pre-\r\nhensile tail is un-\r\ncommon.", "A prehensile tail is uncommon.")
}
