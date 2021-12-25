package org.clulab.pdf2txt.preprocessor

import org.clulab.pdf2txt.LanguageModel
import org.clulab.pdf2txt.common.utils.{TextRange, TextRanges}
import org.clulab.pdf2txt.document.logical.{DocumentBySentence, WordDocument}

import scala.collection.mutable

class WordBreakPreprocessor extends Preprocessor {

  def isSpace(textRange: TextRange): Boolean = textRange.matches(" ")

  def isSeparatedBySingleSpace(prevWordDocument: WordDocument, nextWordDocument: WordDocument): Boolean =
      prevWordDocument.postSeparatorOpt.exists(isSpace)

  def shouldJoin(left: String, right: String, prevWords: Seq[String]): Boolean =
    WordBreakPreprocessor.languageModel.shouldJoin(left, right, prevWords)

  def preprocess(textRange: TextRange): TextRanges = {
    val document = new DocumentBySentence(None, textRange)
    val textRanges = new TextRanges()

    textRanges += document.preSeparatorOpt
    document.contents.foreach { sentence =>
      val prevProcessorWords = new mutable.ArrayBuffer[String]
      var joined = false

      textRanges += sentence.preSeparatorOpt
      sentence.byWordPairOpt.foreach {
        case (None, Some(_)) => // Skip this because we don't know if there are more words.
        case (Some(prevWord), Some(nextWord)) =>
          if (joined) joined = false // Skip next word, but just once.
          else {
            val prevProcessorWord = prevWord.processorsWord
            val nextProcessorWord = nextWord.processorsWord

            if (isSeparatedBySingleSpace(prevWord, nextWord) && shouldJoin(prevProcessorWord, nextProcessorWord, prevProcessorWords)) {
              val processorWord = prevProcessorWord + nextProcessorWord

              prevProcessorWords += processorWord
              textRanges += prevWord.preSeparatorOpt
              textRanges += TextRange(processorWord) // Only joined words are converted to processorWords.
              textRanges += nextWord.postSeparatorOpt
              joined = true
            }
            else {
              prevProcessorWords += prevProcessorWord
              textRanges += prevWord
            }
          }
        case (Some(prevWord), None) => if (!joined) textRanges += prevWord
        case (None, None) =>
      }
      textRanges += sentence.postSeparatorOpt
    }
    textRanges += document.postSeparatorOpt
  }
}

object WordBreakPreprocessor {
  val languageModel = new LanguageModel()
}
