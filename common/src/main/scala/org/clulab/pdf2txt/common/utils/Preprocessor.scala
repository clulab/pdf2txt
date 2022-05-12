package org.clulab.pdf2txt.common.utils

trait Preprocessor extends {
  def preprocess(textRange: TextRange): TextRanges

  def preprocess(text: String): TextRanges = preprocess(TextRange(text))
}
