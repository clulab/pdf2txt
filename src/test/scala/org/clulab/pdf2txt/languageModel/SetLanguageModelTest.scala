package org.clulab.pdf2txt.languageModel

import org.clulab.pdf2txt.common.utils.{Test, TextRange}

class SetLanguageModelTest extends Test {

  behavior of "SetLanguageModel"

  it should "know its own words" in {
    val word = "abcxyz"
    val words = LocalSetLanguageModel(TextRange(word)).words

    words should contain (word)
    words should not contain ("abc")
  }
}
