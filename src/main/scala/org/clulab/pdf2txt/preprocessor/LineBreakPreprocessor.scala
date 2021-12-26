package org.clulab.pdf2txt.preprocessor

import org.clulab.pdf2txt.common.utils.{StringUtils, TextRange, TextRanges, TripleIndexedSeq}
import org.clulab.pdf2txt.document.logical.{DocumentBySentence, SentenceDocument, WordDocument}

class LineBreakPreprocessor extends Preprocessor {

  def isHyphen(wordDocument: WordDocument): Boolean = wordDocument.contents.head.matches("-")

  def isSeparatedBySingleLine(wordDocument: WordDocument): Boolean = isSingleNewline(wordDocument.postSeparatorOpt.get)

  def isSingleNewline(textRange: TextRange): Boolean = StringUtils.LINE_BREAK_STRINGS.exists(textRange.matches)

  def preprocessSentence(sentence: SentenceDocument): TextRanges = {
    val tripleIndexOpt = TripleIndexedSeq(sentence.contents.indices).find { case (prevIndex, currIndex, nextIndex) =>
      val prevWord = sentence.contents(prevIndex) // should have no separator before hyphen
      val currWord = sentence.contents(currIndex) // the hyphen followed by a newline

      isHyphen(currWord) && isSeparatedBySingleLine(currWord) && prevWord.postSeparatorOpt.get.isEmpty
    }

    tripleIndexOpt.map { case (prevIndex, currIndex, nextIndex) =>
      val prevWord = sentence.contents(prevIndex)
      val nextWord = sentence.contents(nextIndex)
      val textRanges = new TextRanges()

      textRanges += sentence.before(prevWord.preSeparatorOpt.get)
      textRanges += prevWord.preSeparatorOpt.get
      textRanges += prevWord.contents.head
      textRanges += nextWord.contents.head
      textRanges += nextWord.postSeparatorOpt.get
      textRanges += sentence.after(nextWord.postSeparatorOpt.get)

      preprocess(TextRange(textRanges.toString))
    }.getOrElse(TextRanges(sentence))
  }

  def preprocess(textRange: TextRange): TextRanges = {
    val document = new DocumentBySentence(None, textRange)
    val textRanges = new TextRanges()

    textRanges += document.preSeparatorOpt
    document.contents.foreach { sentence =>
      textRanges ++= preprocessSentence(sentence)
    }
    textRanges += document.postSeparatorOpt
  }
}
