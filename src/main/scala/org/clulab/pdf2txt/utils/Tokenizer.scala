package org.clulab.pdf2txt.utils

import org.clulab.dynet.Utils
import org.clulab.pdf2txt.common.utils.TextRange
import org.clulab.processors.clu.CluProcessor
import org.clulab.processors.Sentence

class Tokenizer(processor: CluProcessor) {

  // Should this be Array[Array[String]] for words or raw?
  def tokenize(textRange: TextRange, restoreCase: Boolean = false): Array[Sentence] = {
    val document = processor.mkDocument(textRange.toString, keepText = false)

    if (restoreCase) processor.restoreCase(document)
    document.sentences
  }
}

object Tokenizer {
  lazy val defaultTokenizer: Tokenizer = {
    Utils.initializeDyNet()

    new Tokenizer(new CluProcessor())
  }
}