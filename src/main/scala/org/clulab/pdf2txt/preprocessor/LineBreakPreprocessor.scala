package org.clulab.pdf2txt.preprocessor

import org.clulab.pdf2txt.common.utils.{StringUtils, TextRange, TextRanges}
import org.clulab.pdf2txt.document.logical.{DocumentBySentence, WordDocument}

class LineBreakPreprocessor extends Preprocessor {

  def isHyphenated(text: String): Boolean = text.endsWith("-")

  def separatedBySingleLine(wordDocument: WordDocument): Boolean = isSingleNewline(wordDocument.postSeparatorOpt.get)

  def isSingleNewline(textRange: TextRange): Boolean = StringUtils.LINE_BREAK_STRINGS.exists(textRange.matches)

  def preprocess(textRange: TextRange): TextRanges = {
    val document = new DocumentBySentence(None, textRange)
    val textRanges = new TextRanges()

    textRanges += document.preSeparatorOpt
    document.contents.foreach { sentence =>
      textRanges += sentence.preSeparatorOpt

      sentence.byWordPairOpt.foreach {
        case (None, Some(_)) => // Skip because we don't know if there are more words.
        case (Some(prevWord), Some(_)) =>
        // We have to convert to processor's word here, at least if there is hyphenation.
        val prevProcessorsWord = prevWord.processorsWord

        if (isHyphenated(prevProcessorsWord) && prevProcessorsWord.length > 1 && separatedBySingleLine(prevWord))
          textRanges += TextRange(prevProcessorsWord).withoutLast // and no separator
        else
          textRanges += prevWord
        case (Some(prevWord), None) => textRanges += prevWord
        case (None, None) =>
      }
      textRanges += sentence.postSeparatorOpt
    }
    textRanges += document.postSeparatorOpt
  }
}
