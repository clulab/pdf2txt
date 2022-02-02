package org.clulab.pdf2txt.preprocessor

import org.clulab.pdf2txt.common.utils.Test

class TestNumbersPreprocessor extends Test {
  val preprocessor = new NumbersPreprocessor()

  behavior of "NumbersPreprocessor"

  def test(inputText: String, expectedOutputText: String): Unit = {
    it should s"convert ${escape(inputText)}" in {
      val actualOutputText = preprocessor.preprocess(inputText).toString

      actualOutputText shouldBe expectedOutputText
    }
  }

  test("one hello two hello three", "one hello there two hello there three")
//  test("at 42 ,807 ha", "at 42,807 ha")
//  test(
//    "To date , the seedlings are evaluated at 42 ,807 ha  for this current campaign against 49,356 ha in SSC 2020 at the same period , a decrease of 6,549 ha in cultivated areas .",
//    "To date , the seedlings are evaluated at 42,807 ha  for this current campaign against 49,356 ha in SSC 2020 at the same period , a decrease of 6,549 ha in cultivated areas ."
//  )
  test("at 42, 807 ha", "at 42, 807 ha")
  test("at 42 , 807 ha", "at 42 , 807 ha")
  test("at 4d2 ,807 ha", "at 4d2 ,807 ha")
  test("at 42 ,8h07 ha", "at 42 ,8h07 ha")
}
