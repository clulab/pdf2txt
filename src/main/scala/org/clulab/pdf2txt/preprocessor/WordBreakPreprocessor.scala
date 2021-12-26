package org.clulab.pdf2txt.preprocessor

import org.clulab.pdf2txt.LanguageModel
import org.clulab.pdf2txt.common.utils.{PairIndexedSeq, TextRange, TextRanges}
import org.clulab.pdf2txt.document.logical.{DocumentBySentence, SentenceDocument, WordDocument}

class WordBreakPreprocessor(languageModel: LanguageModel = LanguageModel.instance) extends Preprocessor {

  def isSeparatedBySingleSpace(prevWordDocument: WordDocument, nextWordDocument: WordDocument): Boolean =
      prevWordDocument.postSeparator.matches(" ")

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

      textRanges += sentence.before(prevWord.preSeparator)
      textRanges += prevWord.preSeparator
      textRanges += TextRange(processorsWord)
      textRanges += nextWord.postSeparator
      textRanges += sentence.after(nextWord.postSeparator)

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

object WordBreakPreprocessor {
  val languageModel = new LanguageModel()
}
