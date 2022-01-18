package org.clulab.pdf2txt.preprocessor

import org.clulab.pdf2txt.common.utils.{StringUtils, TextRange, TextRanges, TripleIndexedSeq}
import org.clulab.pdf2txt.document.logical.{DocumentBySentence, SentenceDocument, WordDocument}
import org.clulab.pdf2txt.languageModel.{AlwaysLanguageModel, LanguageModel}

class LineBreakPreprocessor(languageModel: LanguageModel = LineBreakPreprocessor.languageModel) extends Preprocessor {

  def isHyphen(wordDocument: WordDocument): Boolean = StringUtils.WORD_BREAK_CHARS.exists(wordDocument.contents.head.matches)

  def isSeparatedBySingleLine(wordDocument: WordDocument): Boolean = isSingleNewline(wordDocument.postSeparator)

  def isSingleNewline(textRange: TextRange): Boolean = StringUtils.LINE_BREAK_STRINGS.exists(textRange.matches)

  def preprocessSentence(sentence: SentenceDocument): TextRanges = {
    val processorsWords = sentence.contents.map(_.processorsWord)
    val tripleIndexOpt = TripleIndexedSeq(sentence.contents.indices).find { case (prevIndex, hyphenIndex, nextIndex) =>
      val prevWord = sentence.contents(prevIndex) // should have no separator before hyphen
      val hyphenWord = sentence.contents(hyphenIndex) // the hyphen followed by a newline
      val nextWord = sentence.contents(nextIndex)

      isHyphen(hyphenWord) && isSeparatedBySingleLine(hyphenWord) && prevWord.postSeparator.isEmpty &&
          languageModel.shouldJoin(prevWord.processorsWord, nextWord.processorsWord, processorsWords.take(prevIndex))
    }

    tripleIndexOpt.map { case (prevIndex, hyphenIndex, nextIndex) =>
      val prevWord = sentence.contents(prevIndex)
      val nextWord = sentence.contents(nextIndex)
      val textRanges = new TextRanges()

      textRanges += sentence.andBefore(prevWord.preSeparator)
      textRanges ++= prevWord.contents
      textRanges ++= nextWord.contents
      textRanges += sentence.andAfter(nextWord.postSeparator)

      preprocess(TextRange(textRanges.toString))
    }.getOrElse(TextRanges(sentence))
  }

  def preprocess(textRange: TextRange): TextRanges = {
    val document = DocumentBySentence(textRange)
    val textRanges = new TextRanges()

    textRanges += document.preSeparator
    document.contents.foreach { sentence =>
      textRanges ++= preprocessSentence(sentence)
    }
    textRanges += document.postSeparator
  }
}


object LineBreakPreprocessor {
  val languageModel = new AlwaysLanguageModel()
}
