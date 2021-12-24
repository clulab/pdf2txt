package org.clulab.pdf2txt.preprocessor

import org.clulab.pdf2txt.LanguageModel
import org.clulab.pdf2txt.common.utils.TextRange
import org.clulab.pdf2txt.document.logical.DocumentBySentence

import scala.collection.mutable

class WordBreakPreprocessor extends Preprocessor {

  def isSpace(textRange: TextRange): Boolean = textRange.matches(" ")

  def shouldMerge(left: String, right: String, prev: Seq[String]): Boolean = true

  def preprocess(textRange: TextRange): Seq[TextRange] = {
    val document = new DocumentBySentence(None, textRange)
    val textRanges = new mutable.ArrayBuffer[TextRange]()

    textRanges += document.preSeparator
    document.bySentence.foreach { sentence =>
      val words = new mutable.ArrayBuffer[String]
      var joined = false

      textRanges += sentence.preSeparator // These should be annealed, even if different words but ranged match up
      sentence.content.byWordPair.foreach { case (prevWord, nextWord) =>
        if (joined)
          joined = false
        else {
          val prevProcessorWord = prevWord.content.processorsWord

          if (isSpace(prevWord.separator.textRange)) {
            val nextProcessorWord = nextWord.content.processorsWord

            if (WordBreakPreprocessor.languageModel.shouldJoin(prevProcessorWord, nextProcessorWord, words)) {
              val processorWord = prevProcessorWord + nextProcessorWord

              words += processorWord
              textRanges += TextRange(processorWord)
              textRanges += nextWord.separator.textRange

              joined = true
            } else {
              words += prevProcessorWord
              textRanges += prevWord.content.textRange
              textRanges += prevWord.separator.textRange
            }
          }
          else {
            words += prevProcessorWord
            textRanges += prevWord.content.textRange
            textRanges += prevWord.separator.textRange
          }
        }
      }
      // A sentence must have at least one word, so there is certainly one left over.
      if (!joined) {
        textRanges += sentence.content.words.last.content.textRange
        textRanges += sentence.content.words.last.separator.textRange
      }
      textRanges += sentence.postSeparator
      textRanges += sentence.separator.textRange
    }
    textRanges += document.postSeparator
  }
}

object WordBreakPreprocessor {
  val languageModel = new LanguageModel()
}
