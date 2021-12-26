package org.clulab.pdf2txt.preprocessor

import org.clulab.pdf2txt.common.utils.Test

class TestLineBreakPreprocessor extends Test {
  val preprocessor = new LineBreakPreprocessor()

  behavior of "LineBreakPreprocessor"

  def test(inputText: String, expectedOutputText: String): Unit = {
    it should s"convert $inputText" in {
      val actualOutputText = preprocessor.preprocess(inputText).toString

      actualOutputText shouldBe expectedOutputText
    }
  }

//  test("1 2 3", "1 2 3")
//  test("- 2 3", "- 2 3")
//  test("- 2 -", "- 2 -")

//  test("1\n2\n3", "1\n2\n3")
//  test("-\n2\n3", "-\n2\n3")
//  test("-\n2\n-", "-\n2\n-")

//  test("-1\n2\n3", "-1\n2\n3")
  test("a pre-\nhensile\ntail", "a prehensile\ntail")
//  test("1-\n2\n-", "12\n-")

  // Show 1-\n2-\n3 only does the first hyphen then if go again can get the second one.
}
