package org.clulab.pdf2txt.preprocessor

import org.clulab.pdf2txt.common.utils._
import org.clulab.pdf2txt.document.logical.{DocumentBySentence, SentenceDocument, WordDocument}
import org.clulab.pdf2txt.languageModel.{GigawordLanguageModel, LanguageModel}

class LineWrapPreprocessor(languageModel: LanguageModel = LineWrapPreprocessor.languageModel) extends Preprocessor {

  def isHyphen(wordDocument: WordDocument): Boolean = StringUtils.WORD_BREAK_CHARS.exists(wordDocument.contents.head.matches)

  def isSeparated(wordDocument: WordDocument): Boolean = wordDocument.postSeparator.nonEmpty

  def isSeparatedBySingleSpace(prevWordDocument: WordDocument, nextWordDocument: WordDocument): Boolean =
      StringUtils.LETTER_BREAK_CHARS.exists(prevWordDocument.postSeparator.matches)

  def shouldJoin(left: String, middle: String, right: String, prevWords: Seq[String]): Boolean =
      languageModel.shouldJoin(left, middle, right, prevWords, withMiddle = false)

  protected def preprocessSentence(sentence: SentenceDocument): TextRanges = {
    val processorsWords = sentence.contents.map(_.processorsWord)
    val tripleIndexOpt = TripleIndexedSeq(sentence.contents.indices).find { case (prevIndex, hyphenIndex, nextIndex) =>
      val prevWord = sentence.contents(prevIndex)
      val hyphenWord = sentence.contents(hyphenIndex)
      val nextWord = sentence.contents(nextIndex)

      isHyphen(hyphenWord) && !isSeparated(prevWord) && isSeparatedBySingleSpace(hyphenWord, nextWord) &&
          shouldJoin(prevWord.processorsWord, hyphenWord.processorsWord, nextWord.processorsWord, processorsWords.take(prevIndex))
    }

    tripleIndexOpt.map { case (prevIndex, currIndex, nextIndex) =>
      val prevWord = sentence.contents(prevIndex)
      val currWord = sentence.contents(currIndex)
      val nextWord = sentence.contents(nextIndex)
      val processorsWord = prevWord.processorsWord + nextWord.processorsWord
      val textRanges = new TextRanges()

      textRanges += sentence.andBefore(prevWord.preSeparator)
      textRanges += TextRange(processorsWord)
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

object LineWrapPreprocessor {
  lazy val languageModel = GigawordLanguageModel()
  val HYPHEN_STRING = StringUtils.HYPHEN.toString
}
