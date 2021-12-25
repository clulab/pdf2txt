package org.clulab.pdf2txt.preprocessor

import org.clulab.pdf2txt.common.utils.{Test, TextRange}

class TestParagraphPreprocessor extends Test {
  val preprocessor = new ParagraphPreprocessor()

  behavior of "ParagraphPreprocessor"

  def test(inputText: String, expectedOutputText: String): Unit = {
    it should s"convert $inputText" in {
      val actualOutputText = preprocessor.preprocess(inputText).toString

      actualOutputText shouldBe expectedOutputText
    }
  }

  test("p1p2", "p1p2 .")
  test("p1 p2", "p1 p2 .")
  test("p1  p2", "p1  p2 .")

  test("p1\np2", "p1\np2 .")
  test("p1\n\np2", "p1 .\n\np2 .")
  test("p1\n \np2", "p1\n \np2 .")

  test("p1\r\np2", "p1\r\np2 .")
  test("p1\r\n\r\np2", "p1 .\r\n\r\np2 .")
  test("p1\r\n \r\np2", "p1\r\n \r\np2 .")

  test("p1\r\rp2", "p1\r\rp2 .")
}
