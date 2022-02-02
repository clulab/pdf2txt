package org.clulab.pdf2txt.preprocessor

import org.clulab.pdf2txt.common.utils.{TextRange, TextRanges}

import java.util.regex.Pattern

class NumbersPreprocessor extends Preprocessor {

  def preprocess(textRange: TextRange): TextRanges = {
    val matcher = NumbersPreprocessor.pattern.matcher(textRange.text)
    val newText = matcher.replaceAll(NumbersPreprocessor.replacement)

    TextRanges(TextRange(newText))
  }
}

object NumbersPreprocessor {
  val pattern = Pattern.compile("(hello)")
  val replacement = "$1 there"
}
