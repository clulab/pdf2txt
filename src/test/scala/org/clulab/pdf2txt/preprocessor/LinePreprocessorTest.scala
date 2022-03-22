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

  // This is the standard case with the third line starting with a lowercase letter.
  test("2.1. Experimental site, plant materials and initial\n\ngrowth conditions", "2.1. Experimental site, plant materials and initial\ngrowth conditions")
  // In these two cases the lines should remain separated because of how the thrd line begins.
  test("growth conditions\n\nAll experiments were conducted at the research", "growth conditions\n\nAll experiments were conducted at the research")
  test("2. Materials and methods\n\n 2.1. Experimental site, plant materials and initial", "2. Materials and methods\n\n 2.1. Experimental site, plant materials and initial")

  // These cases depend on how the first line ends.
  test("2.1. Experimental site, plant materials and initial.\n\ngrowth conditions", "2.1. Experimental site, plant materials and initial.\n\ngrowth conditions")
  test("2.1. Experimental site, plant materials and initial! \n\ngrowth conditions", "2.1. Experimental site, plant materials and initial! \n\ngrowth conditions")
  test("2.1. Experimental site, plant materials and initial \n\ngrowth conditions", "2.1. Experimental site, plant materials and initial \ngrowth conditions")
}
