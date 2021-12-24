package org.clulab.pdf2txt.preprocessor

import org.clulab.pdf2txt.LanguageModel
import org.clulab.pdf2txt.common.utils.TextRange
import org.clulab.pdf2txt.document.logical.{DocumentBySentence, WordDocument}

import scala.collection.mutable

class WordBreakPreprocessor extends Preprocessor {

  def isSpace(textRange: TextRange): Boolean = textRange.matches(" ")

  def isSeparatedBySingleSpace(wordDocument: WordDocument): Boolean = true

  def shouldMerge(left: String, right: String, prev: Seq[String]): Boolean = true

  def preprocess(textRange: TextRange): Seq[TextRange] = {
    val document = new DocumentBySentence(None, textRange)
    val textRanges = new mutable.ArrayBuffer[TextRange]()

    textRanges ++= document.preSeparatorOpt.toSeq
    document.contents.foreach { sentence =>
      val words = new mutable.ArrayBuffer[String]
      var joined = false

      textRanges ++= sentence.preSeparatorOpt.toSeq
      sentence.byWordPairOpt.foreach {
        case (None, Some(_)) => // Skip because we don't know if there are more words.
        case (Some(prevWord), Some(nextWord)) =>
          if (joined) joined = false // Skip next word, but just once.
          else {
            val prevProcessorWord = prevWord.processorsWord
            val nextProcessorWord = nextWord.processorsWord

            if (isSeparatedBySingleSpace(prevWord) &&
                WordBreakPreprocessor.languageModel.shouldJoin(prevProcessorWord, nextProcessorWord, words)) {
              val processorWord = prevProcessorWord + nextProcessorWord

              words += processorWord
              textRanges += TextRange(processorWord)
              textRanges ++= nextWord.postSeparatorOpt.toSeq
              joined = true
            }
            else {
              words += prevProcessorWord
              textRanges += prevWord
            }
          }
        case (Some(prevWord), None) => if (!joined) textRanges += prevWord
      }
      textRanges ++= sentence.postSeparatorOpt.toSeq
    }
    textRanges ++= document.postSeparatorOpt.toSeq
  }
}

object WordBreakPreprocessor {
  val languageModel = new LanguageModel()
}
