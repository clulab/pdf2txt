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

  behavior of "WordBreakPreprocessor"

  def test(inputText: String, expectedOutputText: String, vocab: Seq[String]): Unit = {
    val languageModel = new TestLanguageModel(vocab)
    val preprocessor = new WordBreakPreprocessor(languageModel)

    it should s"convert $inputText" in {
      val actualOutputText = preprocessor.preprocess(inputText).toString

      actualOutputText shouldBe expectedOutputText
    }
  }

  test("I went in to the store.", "I went into the store.", Seq("into"))
  test("Pre hensile tails are un common.", "Prehensile tails are uncommon.", Seq("Prehensile", "uncommon"))
  test(
    "Once upon a time.  He went in to and on to the house.  They lived happily ever after.",
  "Once upon a time.  He went into and onto the house.  They lived happily ever after.",
    Seq("into", "onto")
  )
}
