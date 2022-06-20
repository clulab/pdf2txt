package org.clulab.pdf2txt.preprocessor

import org.clulab.pdf2txt.common.utils.Test

import java.io.PrintWriter

class NumberPreprocessorTest extends Test {
  val logger = new NumbersLogger(new PrintWriter(System.out))

  behavior of "NumbersPreprocessor on texts with commas"

  {
    val hyperparameters = NumberPreprocessor.Hyperparameters()
    val preprocessor = new NumberPreprocessor(hyperparameters, Some(logger))

    def test(inputText: String, expectedOutputText: String): Unit = {
      it should s"convert ${escape(inputText)}" in {
        val actualOutputText = preprocessor.preprocess(inputText).toString

        actualOutputText shouldBe expectedOutputText
      }
    }

    test("at 42 ,807 ha", "at 42,807 ha")
    test(
      "To date , the seedlings are evaluated at 42 ,807 ha  for this current campaign against 49,356 ha in SSC 2020 at the same period , a decrease of 6,549 ha in cultivated areas .",
      "To date , the seedlings are evaluated at 42,807 ha  for this current campaign against 49,356 ha in SSC 2020 at the same period , a decrease of 6,549 ha in cultivated areas ."
    )
    test("at 42, 807 ha", "at 42, 807 ha")
    test("at 42 , 807 ha", "at 42 , 807 ha")

    // TODO:
    // test("at 4d2 ,807 ha", "at 4d2 ,807 ha")
    // test("at 42 ,8h07 ha", "at 42 ,8h07 ha")
  }

  behavior of "NumbersPreprocessor on texts with spaces"

  {
    val hyperparameters = NumberPreprocessor.Hyperparameters(joinWithSpaces = true)
    val preprocessor = new NumberPreprocessor(hyperparameters, Some(logger))

    def test(inputText: String, expectedOutputText: String): Unit = {
      it should s"convert ${escape(inputText)}" in {
        val actualOutputText = preprocessor.preprocess(inputText).toString

        actualOutputText shouldBe expectedOutputText
      }
    }

    test("+43 01 02 03", "+43 010203")
    test("4123 1234 1234 1234", "4123123412341234")
    test(
      "50 000 ha (SAED, 1997; SONADER, 1998). During the 1998 wet season, about 75% of this area was used, i.e. 20 000 ha in Senegal and 18 000 ha in Mauritania, almost exclusively for",
      "50000 ha (SAED, 1997; SONADER, 1998). During the 1998 wet season, about 75% of this area was used, i.e. 20000 ha in Senegal and 18000 ha in Mauritania, almost exclusively for"
    )
  }
}
