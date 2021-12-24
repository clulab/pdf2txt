package org.clulab.pdf2txt.preprocessor

import org.clulab.pdf2txt.common.utils.TextRange
import org.clulab.pdf2txt.document.logical.{DocumentBySentence, Word}

import scala.collection.mutable

class LineBreakPreprocessor extends Preprocessor {

  def isHyphenated(text: String): Boolean = text.endsWith("-")

  def separatedBySingleLine(word: Word): Boolean = isSingleNewline(word.separator)

  def isSingleNewline(textRange: TextRange): Boolean = textRange.matches("\n")

  def preprocess(textRange: TextRange): Seq[TextRange] = {
    val document = new DocumentBySentence(None, textRange)
    val textRanges = new mutable.ArrayBuffer[TextRange]()

    textRanges += document.preSeparator
    document.bySentence.foreach { sentence =>
      textRanges += sentence.preSeparator
      sentence.content.byWordPair.foreach { case (prevWord, _) =>
        // We have to convert to processor's word here, at least if there is hyphenation.
        val processorWord = prevWord.content.processorsWord

        if (isHyphenated(processorWord) && processorWord.length > 1 && separatedBySingleLine(prevWord)) {
          textRanges += TextRange(processorWord)
        }
        else {
          textRanges += TextRange(processorWord)
          textRanges += prevWord.separator
        }
      }
      // A sentence must have at least one word, so there is certainly one left over.
      textRanges += TextRange(sentence.content.words.last.content.processorsWord)
      textRanges += sentence.content.words.last.separator
      textRanges += sentence.postSeparator
      textRanges += sentence.separator
    }
    textRanges += document.postSeparator
  }
}
