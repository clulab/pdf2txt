package org.clulab.pdf2txt.preprocessor

import org.clulab.pdf2txt.common.utils.{StringUtils, TextRange, TextRanges, TripleIndexedSeq}
import org.clulab.pdf2txt.document.logical.{DocumentBySentence, SentenceDocument, WordDocument}
import org.clulab.pdf2txt.languageModel.{LanguageModel, NeverLanguageModel, ProbabilisticLanguageModel}

class WordBreakByHyphenPreprocessor(languageModel: LanguageModel = WordBreakByHyphenPreprocessor.languageModel) extends Preprocessor {

  def isHyphen(wordDocument: WordDocument): Boolean = StringUtils.WORD_BREAK_CHARS.exists(wordDocument.contents.head.matches)

  def isSeparated(wordDocument: WordDocument): Boolean = wordDocument.postSeparator.nonEmpty

  def isSeparatedBySingleSpace(prevWordDocument: WordDocument, nextWordDocument: WordDocument): Boolean =
      StringUtils.LETTER_BREAK_CHARS.exists(prevWordDocument.postSeparator.matches)

  def shouldJoin(left: String, right: String, prevWords: Seq[String]): Boolean =
      languageModel.shouldJoin(left, right, prevWords)

  protected def preprocessSentence(sentence: SentenceDocument): TextRanges = {
    val processorsWords = sentence.contents.map(_.processorsWord)
    val tripleIndexOpt = TripleIndexedSeq(sentence.contents.indices).find { case (prevIndex, hyphenIndex, nextIndex) =>
      val prevWord = sentence.contents(prevIndex)
      val hyphenWord = sentence.contents(hyphenIndex)
      val nextWord = sentence.contents(nextIndex)

      isHyphen(hyphenWord) && !isSeparated(prevWord) && isSeparatedBySingleSpace(hyphenWord, nextWord) &&
          shouldJoin(prevWord.processorsWord, nextWord.processorsWord, processorsWords.take(prevIndex))
    }

    tripleIndexOpt.map { case (prevIndex, currIndex, nextIndex) =>
      val prevWord = sentence.contents(prevIndex)
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

object WordBreakByHyphenPreprocessor {
  val languageModel = new NeverLanguageModel()
}
