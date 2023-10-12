package org.clulab.pdf2txt.utils

import org.clulab.pdf2txt.common.utils.TextRange
import org.clulab.processors.clu.BalaurProcessor
import org.clulab.processors.Sentence
import org.clulab.utils.Lazy

class Tokenizer(val processor: BalaurProcessor) {

  def tokenize(textRange: TextRange, restoreCase: Boolean = false): Array[Sentence] = {
    val document = processor.mkDocument(textRange.toString, keepText = false)

    // TODO: This does not presently exist!
    // if (restoreCase) processor.restoreCase(document)
    document.sentences
  }
}

object Tokenizer {
  // A lazy must be a val, not a var.
  protected var lazyTokenizer: Lazy[Tokenizer] = Lazy{
    new Tokenizer(new BalaurProcessor())
  }

  def getTokenizer: Tokenizer = synchronized {
    lazyTokenizer.value
  }

  def setTokenizer(tokenizer: Tokenizer): Unit = synchronized {
    lazyTokenizer = Lazy(tokenizer)
  }

  def getProcessor: BalaurProcessor = getTokenizer.processor
}
