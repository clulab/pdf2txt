package org.clulab.pdf2txt.preprocessor

import org.clulab.pdf2txt.common.utils.Test
import org.clulab.pdf2txt.languageModel.ProbabilisticLanguageModel

class TestWordBreakBySpacePreprocessor extends Test {

  class TestLanguageModel(vocab: Map[String, Float]) extends ProbabilisticLanguageModel {

    override def p(nextWord: String, prevWords: Seq[String]): Float  = {
      // val context = prevWords.mkString(" ")

      // println(s"p($nextWord | $context)")
      vocab.getOrElse(nextWord, 0)
    }
  }

  behavior of "WordBreakBySpacePreprocessor"

  def test(inputText: String, expectedOutputText: String, vocab: Map[String, Float]): Unit = {
    val languageModel = new TestLanguageModel(vocab)
    val preprocessor = new WordBreakBySpacePreprocessor(languageModel)

    it should s"convert $inputText" in {
      val actualOutputText = preprocessor.preprocess(inputText).toString

      actualOutputText shouldBe expectedOutputText
    }
  }

  test("I went in to the store.", "I went into the store.", Map("into" -> 1))
  test("Pre hensile tails are un common.", "Prehensile tails are uncommon.", Map("Prehensile" -> 1, "uncommon" -> 2))
  test(
    "Once upon a time.  He went in to and on to the house.  They lived happily ever after.",
    "Once upon a time.  He went into and onto the house.  They lived happily ever after.",
    Map("into" -> 1f, "onto" -> 2f)
  )
  test(
    "It is a double triple quadruple threat.",
    "It is a doubletriplequadruple threat.",
    Map("doubletriple" -> 1f, "doubletriplequadruple" -> 2f)
  )
}
