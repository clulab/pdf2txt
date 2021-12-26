package org.clulab.pdf2txt.preprocessor

import org.clulab.pdf2txt.LanguageModel
import org.clulab.pdf2txt.common.utils.{PairIndexedSeq, TextRange, TextRanges}
import org.clulab.pdf2txt.document.logical.{DocumentBySentence, SentenceDocument, WordDocument}

class WordBreakPreprocessor(languageModel: LanguageModel = LanguageModel.instance) extends Preprocessor {

  def isSpace(textRange: TextRange): Boolean = textRange.matches(" ")

  def isSeparatedBySingleSpace(prevWordDocument: WordDocument, nextWordDocument: WordDocument): Boolean =
      prevWordDocument.postSeparatorOpt.exists(isSpace)

  def shouldJoin(left: String, right: String, prevWords: Seq[String]): Boolean =
      languageModel.shouldJoin(left, right, prevWords)

  protected def preprocessSentence(sentence: SentenceDocument): TextRanges = {
    val processorsWords = sentence.contents.map(_.processorsWord)
    val pairIndexOpt = PairIndexedSeq(sentence.contents.indices).find { case (prevIndex, nextIndex) =>
      val prevWord = sentence.contents(prevIndex)
      val nextWord = sentence.contents(nextIndex)

      isSeparatedBySingleSpace(prevWord, nextWord) && shouldJoin(prevWord.processorsWord, nextWord.processorsWord, processorsWords.take(prevIndex))
    }

    pairIndexOpt.map { case (prevIndex, nextIndex) =>
      val prevWord = sentence.contents(prevIndex)
      val nextWord = sentence.contents(nextIndex)
      val processorsWord = prevWord.processorsWord + nextWord.processorsWord
      val textRanges = new TextRanges()

      textRanges += sentence.before(prevWord.preSeparatorOpt.get)
      textRanges += prevWord.preSeparatorOpt.get
      textRanges += TextRange(processorsWord)
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

object WordBreakPreprocessor {
  val languageModel = new LanguageModel()
}
