package org.clulab.pdf2txt.languageModel

import org.clulab.pdf2txt.common.utils.Test

class TestGigawordLanguageModel extends Test {

  behavior of "GigawordLanguageModel"

  it should "load" in {
    val gigaword = GigawordLanguageModel()

    gigaword.wordFrequencies.size should be (3692062)
    gigaword.wordFrequencies.values.sum should be (2066074345)
  }
}
