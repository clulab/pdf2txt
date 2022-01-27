package org.clulab.pdf2txt.preprocessor

import org.clulab.pdf2txt.common.utils.{DoubleIndexedSeq, StringUtils, TextRange, TextRanges}
import org.clulab.pdf2txt.document.logical.{DocumentBySentence, SentenceDocument, WordDocument}
import org.clulab.pdf2txt.languageModel.{LanguageModel, ProbabilisticLanguageModel}

class WordBreakBySpacePreprocessor(languageModel: LanguageModel = WordBreakBySpacePreprocessor.languageModel) extends Preprocessor {

  def isSeparatedBySingleSpace(prevWordDocument: WordDocument, nextWordDocument: WordDocument): Boolean =
      StringUtils.LETTER_BREAK_CHARS.exists(prevWordDocument.postSeparator.matches)

  def shouldJoin(left: String, right: String, prevWords: Seq[String]): Boolean =
      languageModel.shouldJoin(left, right, prevWords)

  protected def preprocessSentence(sentence: SentenceDocument): TextRanges = {
    val processorsWords = sentence.contents.map(_.processorsWord)
    val pairIndexOpt = DoubleIndexedSeq(sentence.contents.indices).find { case (prevIndex, nextIndex) =>
      val prevWord = sentence.contents(prevIndex)
      val nextWord = sentence.contents(nextIndex)

      isSeparatedBySingleSpace(prevWord, nextWord) && shouldJoin(prevWord.processorsWord, nextWord.processorsWord, processorsWords.take(prevIndex))
    }

    pairIndexOpt.map { case (prevIndex, nextIndex) =>
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

object WordBreakBySpacePreprocessor {
  val languageModel = ProbabilisticLanguageModel.instance
}
