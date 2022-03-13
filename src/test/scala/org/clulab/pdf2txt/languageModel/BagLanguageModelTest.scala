package org.clulab.pdf2txt.languageModel

import org.clulab.pdf2txt.common.utils.{Test, TextRange}

class BagLanguageModelTest extends Test {

  behavior of "BagLanguageModel"

  it should "know its own words" in {
    val word = "Abcxyz is a Fabulous a thing."
    val wordFrequencies = LocalBagLanguageModel(TextRange(word)).wordFrequencies

    wordFrequencies("abc") should be (0)
    wordFrequencies("abcxyz") should be (0)
    wordFrequencies("Abcxyz") should be (1)
    wordFrequencies("is") should be (1)
    wordFrequencies("a") should be (2)
    wordFrequencies("Fabulous") should be (1)
  }
}
