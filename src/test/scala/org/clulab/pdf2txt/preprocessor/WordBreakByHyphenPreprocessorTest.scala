package org.clulab.pdf2txt.preprocessor

import org.clulab.pdf2txt.common.utils.Test
import org.clulab.pdf2txt.languageModel.ProbabilisticLanguageModel

class WordBreakByHyphenPreprocessorTest extends Test {

  class TestLanguageModel(vocab: Map[String, Float]) extends ProbabilisticLanguageModel {

    override def p(nextWord: String, prevWords: Seq[String]): Float  = {
      val context = prevWords.mkString(" ")
      val probability = vocab.getOrElse(nextWord, 0f)

      println(s"p($nextWord | $context) = $probability")
      probability
    }
  }

  behavior of "WordBreakByHyphenPreprocessor"

  def test(inputText: String, expectedOutputText: String, vocab: Map[String, Float]): Unit = {
    val languageModel = new TestLanguageModel(vocab)
    val preprocessor = new WordBreakByHyphenPreprocessor(languageModel)

    it should s"convert $inputText" in {
      val actualOutputText = preprocessor.preprocess(inputText).toString

      actualOutputText shouldBe expectedOutputText
    }
  }

  test(
    "Differences between left- and right- handedness.",
    "Differences between left- and right-handedness.",
    Map("right-handedness" -> 1)
  )
  test(
    "It is a five- year- old bicycle.",
    "It is a five-year-old bicycle.",
    Map("five-year" -> 1f, "five-year-old" -> 1f)
  )
  test(
    "It is a five-year- old bicycle.",
    "It is a five-year-old bicycle.",
    Map("five-year" -> 1f, "five-year-old" -> 1f)
  )
}
