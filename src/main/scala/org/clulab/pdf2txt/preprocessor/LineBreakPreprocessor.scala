package org.clulab.pdf2txt.preprocessor

import org.clulab.pdf2txt.common.utils.StringUtils._
import org.clulab.pdf2txt.document.logical.DocumentBySentence

class LineBreakPreprocessor extends Preprocessor {

  def preprocess(rawText: String, range: Range, stringBuilder: StringBuilder): Unit = {
    val document = DocumentBySentence(rawText)

    stringBuilder ++ document.preSeparator
    document.bySentence.foreach { sentence =>
      stringBuilder ++ rawText.substring(sentence.preRange)
      // Know that there is at least a next word to be printed.
      sentence.byWordPair.foreach { case (prevWord, _) =>
        if (prevWord.endsWithHyphen && prevWord.separatedBySingleLine) {
          stringBuilder ++ prevWord.wordContent.toString // without last
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
