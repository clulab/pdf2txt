package org.clulab.pdf2txt.languageModel

import org.clulab.pdf2txt.common.utils.Test

class GigawordLanguageModelTest extends Test {

  behavior of "GigawordLanguageModel"

  it should "load" in {
    val gigaword = GigawordLanguageModel()

    gigaword.wordFrequencies.size should be (3692061)
    gigaword.wordFrequencies.values.sum should be (2066073892)

    gigaword.wordFrequencies("a") should be > (1)
    gigaword.wordFrequencies("") should be (0)
  }
}
