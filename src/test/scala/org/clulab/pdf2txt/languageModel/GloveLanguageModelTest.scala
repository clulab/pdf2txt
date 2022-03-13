package org.clulab.pdf2txt.languageModel

import org.clulab.pdf2txt.common.utils.Test

class GloveLanguageModelTest extends Test {

  behavior of "GloveLanguageModel"

  it should "load" in {
    val glove = GloveLanguageModel()

    glove.words.size should be (2196016)
    glove.words("a") should be (true)
    glove.words("") should be (false)
  }
}
