package org.clulab.pdf2txt.preprocessor

import org.clulab.pdf2txt.common.utils.Test
import org.clulab.pdf2txt.languageModel.ProbabilisticLanguageModel

class LigaturePreprocessorTest extends Test {

  class TestLanguageModel(vocab: Map[String, Float]) extends ProbabilisticLanguageModel {

    override def p(nextWord: String, prevWords: Seq[String]): Float  = {
      val context = prevWords.mkString(" ")
      val probability = vocab.getOrElse(nextWord, 0f)

      println(s"p($nextWord | $context) = $probability")
      probability
    }
  }

  behavior of "LigaturePreprocessor"

  def test(inputText: String, expectedOutputText: String, vocab: Map[String, Float]): Unit = {
    val languageModel = new TestLanguageModel(vocab)
    val preprocessor = new LigaturePreprocessor(languageModel)

    it should s"convert $inputText" in {
      val actualOutputText = preprocessor.preprocess(inputText).toString

      actualOutputText shouldBe expectedOutputText
    }
  }

  test(
    "fl our is not fl avored ij s but waffl es are, go fi gure, said at the cliff side.",
    "flour is not flavored ij s but waffl es are, go fi gure, said at the cliffside.",
    Map(
      "flour" -> 1, "flavored" -> 1, "ijs" -> 1, "waffles" -> 0.8f, "cliffside" -> 1,
      "waffl" -> 0.9f
    )
  )
  test (
    "A coe ffi cient is not dif fi cult to defi ne.",
    "A coefficient is not difficult to define.",
    Map("coefficient" -> 1, "difficult" -> 1, "define" -> 1)
  )
}
