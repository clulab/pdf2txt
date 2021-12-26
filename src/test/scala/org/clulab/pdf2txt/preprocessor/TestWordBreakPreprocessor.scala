package org.clulab.pdf2txt.preprocessor

import org.clulab.pdf2txt.LanguageModel
import org.clulab.pdf2txt.common.utils.Test

class TestWordBreakPreprocessor extends Test {

  class TestLanguageModel(vocab: Seq[String]) extends LanguageModel {

    override def p(nextWord: String, prevWords: Seq[String]): Float  = {
      val context = prevWords.mkString(" ")

      println(s"p($nextWord | $context)")
      if (vocab.contains(nextWord)) 1 else 0
    }
  }

  val languageModel = new TestLanguageModel(Seq("into"))
  val preprocessor = new WordBreakPreprocessor(languageModel)

  behavior of "WordBreakPreprocessor"

  def test(inputText: String, expectedOutputText: String): Unit = {
    it should s"convert $inputText" in {
      val actualOutputText = preprocessor.preprocess(inputText).toString

      actualOutputText shouldBe expectedOutputText
    }
  }

  test("I went in to the store.", "I went into the store.")

}
