package org.clulab.pdf2txt.utils

import org.clulab.pdf2txt.common.utils.TextRange
import org.clulab.processors.clu.BalaurProcessor
import org.clulab.processors.{Processor, Sentence}
import org.clulab.utils.Lazy

class Tokenizer(processor: Processor) {

  def tokenize(textRange: TextRange, restoreCase: Boolean = false): Array[Sentence] = {
    val document = processor.mkDocument(textRange.toString, keepText = false)

    // TODO: This does not presently exist!
    // if (restoreCase) processor.restoreCase(document)
    document.sentences
  }
}

object Tokenizer {
  // A lazy must be a val, not a var.
  var lazyTokenizer: Lazy[Tokenizer] = Lazy{
    new Tokenizer(new BalaurProcessor())
  }

  def setTokenizer(tokenizer: Tokenizer): Unit = synchronized {
    lazyTokenizer = Lazy(tokenizer)
  }
}
