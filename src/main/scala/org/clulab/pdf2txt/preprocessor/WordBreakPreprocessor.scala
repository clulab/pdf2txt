package org.clulab.pdf2txt.preprocessor

import org.clulab.pdf2txt.common.utils.StringUtils._
import org.clulab.pdf2txt.document.logical.DocumentBySentence

class WordBreakPreprocessor extends Preprocessor {

  def preprocess(rawText: String, range: Range, stringBuilder: StringBuilder): Unit = {
    val document = DocumentBySentence(rawText)

    stringBuilder ++ document.preSeparator
    document.bySentence.foreach { sentence =>
      stringBuilder ++ rawText.substring(sentence.preRange)
      sentence.byWordPair.foreach { case (prevWord, nextWord) =>
        if (prevWord.separatedBySpace) {
          if (true) { // should join
            stringBuilder ++ prevWord.wordContent.toString
            // Skip the single space separator then
          } else {
            stringBuilder ++ prevWord.wordContent.toString
            stringBuilder ++ prevWord.wordSeparator.toString
          }
        }
        else {
          stringBuilder ++ prevWord.wordContent.toString
          stringBuilder ++ prevWord.wordSeparator.toString
        }
      }
      stringBuilder ++ sentence.words.last.toString
      stringBuilder ++ rawText.substring(sentence.postRange)
    }
    stringBuilder ++ document.postSeparator
  }
}
