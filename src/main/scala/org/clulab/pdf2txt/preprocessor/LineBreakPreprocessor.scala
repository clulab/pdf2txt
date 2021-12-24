package org.clulab.pdf2txt.preprocessor

import org.clulab.pdf2txt.common.utils.TextRange
import org.clulab.pdf2txt.document.logical.{DocumentBySentence, WordDocument}

import scala.collection.mutable

class LineBreakPreprocessor extends Preprocessor {

  def isHyphenated(text: String): Boolean = text.endsWith("-")

  def separatedBySingleLine(wordDocument: WordDocument): Boolean = isSingleNewline(wordDocument.postSeparatorOpt.get)

  def isSingleNewline(textRange: TextRange): Boolean = textRange.matches("\n")

  def preprocess(textRange: TextRange): Seq[TextRange] = {
    val document = new DocumentBySentence(None, textRange)
    val textRanges = new mutable.ArrayBuffer[TextRange]()

    textRanges ++= document.preSeparatorOpt.toSeq
    document.contents.foreach { sentence =>
      textRanges ++= sentence.preSeparatorOpt.toSeq

      sentence.byWordPairOpt.foreach {
        case (None, Some(_)) => // Skip because we don't know if there are more words.
        case (Some(prevWord), Some(_)) =>
        // We have to convert to processor's word here, at least if there is hyphenation.
        val processorWord = prevWord.processorsWord

        if (isHyphenated(processorWord) && processorWord.length > 1 && separatedBySingleLine(prevWord))
          textRanges += TextRange(processorWord).withoutLast // and no separator
        else
          textRanges += prevWord
        case (Some(prevWord), None) => textRanges += prevWord
      }
      textRanges ++= sentence.postSeparatorOpt.toSeq
    }
    textRanges ++= document.postSeparatorOpt.toSeq
  }
}
