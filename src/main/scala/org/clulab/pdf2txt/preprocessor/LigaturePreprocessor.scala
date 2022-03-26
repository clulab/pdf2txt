package org.clulab.pdf2txt.preprocessor

import org.clulab.pdf2txt.common.utils.{DoubleIndexedSeq, StringUtils, TextRange, TextRanges, TripleIndexedSeq}
import org.clulab.pdf2txt.document.logical.{DocumentBySentence, SentenceDocument, WordDocument}
import org.clulab.pdf2txt.languageModel.{AlwaysLanguageModel, LanguageModel}

class LigaturePreprocessor(languageModel: LanguageModel = LigaturePreprocessor.languageModel) extends Preprocessor {

  def isSeparatedBySingleSpace(prevWordDocument: WordDocument, nextWordDocument: WordDocument): Boolean =
      StringUtils.LETTER_BREAK_CHARS.exists(prevWordDocument.postSeparator.matches)

  def isLigature(string: String): Boolean = StringUtils.endsWithLigature(string)

  def shouldJoin(left: String, right: String, prevWords: Seq[String]): Boolean =
      languageModel.shouldJoin(left, right, prevWords)

  def shouldJoin(left: String, middle: String, right: String, prevWords: Seq[String]): Boolean =
      languageModel.shouldJoin(left, middle, right, prevWords)

  protected def preprocessDoublesSentence(sentence: SentenceDocument): TextRanges = {
    val processorsWords = sentence.contents.map(_.processorsWord)
    val doubleIndexOpt = DoubleIndexedSeq(sentence.contents.indices).find { case (prevIndex, nextIndex) =>
      val prevWord = sentence.contents(prevIndex)
      val nextWord = sentence.contents(nextIndex)

      isSeparatedBySingleSpace(prevWord, nextWord) && isLigature(prevWord.processorsWord) &&
          shouldJoin(prevWord.processorsWord, nextWord.processorsWord, processorsWords.take(prevIndex))
    }

    doubleIndexOpt.map { case (prevIndex, nextIndex) =>
      val prevWord = sentence.contents(prevIndex)
      val nextWord = sentence.contents(nextIndex)
      val processorsWord = prevWord.processorsWord + nextWord.processorsWord
      val textRanges = new TextRanges()

      textRanges += sentence.andBefore(prevWord.preSeparator)
      textRanges += TextRange(processorsWord)
      textRanges += sentence.andAfter(nextWord.postSeparator)

      preprocess(TextRange(textRanges.toString))
    }.getOrElse(TextRanges(sentence)) // Give up and use the sentence as is.
  }

  protected def preprocessTriplesSentence(sentence: SentenceDocument): TextRanges = {
    val processorsWords = sentence.contents.map(_.processorsWord)
    val tripleIndexOpt = TripleIndexedSeq(sentence.contents.indices).find { case (prevIndex, currIndex, nextIndex) =>
      val prevWord = sentence.contents(prevIndex)
      val currWord = sentence.contents(currIndex)
      val nextWord = sentence.contents(nextIndex)

      isSeparatedBySingleSpace(prevWord, currWord) && isSeparatedBySingleSpace(currWord, nextWord) &&
          isLigature(currWord.processorsWord) &&
          shouldJoin(prevWord.processorsWord, currWord.processorsWord, nextWord.processorsWord, processorsWords.take(prevIndex))
    }

    tripleIndexOpt.map { case (prevIndex, currIndex, nextIndex) =>
      val prevWord = sentence.contents(prevIndex)
      val currWord = sentence.contents(currIndex)
      val nextWord = sentence.contents(nextIndex)
      val processorsWord = prevWord.processorsWord + currWord.processorsWord + nextWord.processorsWord
      val textRanges = new TextRanges()

      textRanges += sentence.andBefore(prevWord.preSeparator)
      textRanges += TextRange(processorsWord)
      textRanges += sentence.andAfter(nextWord.postSeparator)

      preprocess(TextRange(textRanges.toString))
    }.getOrElse(preprocessDoublesSentence(sentence)) // Try with just doubles.
  }

  def preprocess(textRange: TextRange): TextRanges = {
    val document = DocumentBySentence(textRange)
    val textRanges = new TextRanges()

    textRanges += document.preSeparator
    document.contents.foreach { sentence =>
      textRanges ++= preprocessTriplesSentence(sentence)
    }
    textRanges += document.postSeparator
  }
}

object LigaturePreprocessor {
  lazy val languageModel = new AlwaysLanguageModel()
}
