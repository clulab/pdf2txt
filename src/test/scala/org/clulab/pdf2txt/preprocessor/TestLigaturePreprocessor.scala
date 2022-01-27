package org.clulab.pdf2txt.preprocessor

import org.clulab.pdf2txt.common.utils.Test
import org.clulab.pdf2txt.languageModel.ProbabilisticLanguageModel

class TestLigaturePreprocessor extends Test {

  class TestLanguageModel(vocab: Map[String, Float]) extends ProbabilisticLanguageModel {

    override def p(nextWord: String, prevWords: Seq[String]): Float  = {
      // val context = prevWords.mkString(" ")

      // println(s"p($nextWord | $context)")
      vocab.getOrElse(nextWord, 0)
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
    "flour is not flavored ij s but waffles are, go fi gure, said at the cliffside.",
    Map("flour" -> 1, "flavored" -> 1, "ijs" -> 1, "waffles" -> 1, "cliffside" ->1))
}
