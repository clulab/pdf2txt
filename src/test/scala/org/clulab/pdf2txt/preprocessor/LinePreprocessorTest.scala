package org.clulab.pdf2txt.preprocessor

import org.clulab.pdf2txt.common.utils.Test

class LinePreprocessorTest extends Test {
  val preprocessor = new LinePreprocessor()

  behavior of "LinePreprocessor"

  def test(inputText: String, expectedOutputText: String): Unit = {
    it should s"convert ${escape(inputText)}" in {
      val actualOutputText = preprocessor.preprocess(inputText).toString

      actualOutputText shouldBe expectedOutputText
    }
  }

  test("2.1. Experimental site, plant materials and initial\n\ngrowth conditions", "2.1. Experimental site, plant materials and initial\ngrowth conditions")
  test("growth conditions\n\nAll experiments were conducted at the research", "growth conditions\n\nAll experiments were conducted at the research")
  test("2. Materials and methods\n\n2.1. Experimental site, plant materials and initial", "2. Materials and methods\n\n2.1. Experimental site, plant materials and initial")
}
