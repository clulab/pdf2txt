package org.clulab.pdf2txt.preprocessor

import org.clulab.pdf2txt.common.utils.{TextRange, TextRanges}

trait Preprocessor extends {
  def preprocess(textRange: TextRange): TextRanges

  def preprocess(text: String): TextRanges = preprocess(TextRange(text))
}
