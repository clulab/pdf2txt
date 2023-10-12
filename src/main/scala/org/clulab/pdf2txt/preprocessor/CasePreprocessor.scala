package org.clulab.pdf2txt.preprocessor

import org.clulab.dynet.Utils
import org.clulab.pdf2txt.common.utils.{Preprocessor, TextRange, TextRanges}
import org.clulab.pdf2txt.document.logical.{DocumentByWord, WordDocument}
import org.clulab.pdf2txt.utils.WordTypes
import org.clulab.processors.Sentence

class CasePreprocessor(cutoff: Float = CasePreprocessor.defaultCutoff) extends Preprocessor {

  def getPercentNotLower(sentence: Sentence): Float = {

    def countWordType(wordType: WordTypes.WordType): Int =
      sentence.words.count(WordTypes(_) == wordType)

    val wordCount = sentence.words.length
    val allLowerCount = countWordType(WordTypes.AllLower)
    val percentNotLower = (wordCount - allLowerCount).toFloat / wordCount * 100

    percentNotLower
  }

  def preprocess(textRange: TextRange): TextRanges = {
    val document = {
      val tokenizer = CasePreprocessor.tokenizer
      val preservedSentences = {
        val sentences = tokenizer.tokenize(textRange)
        sentences
      }
      val restoredSentences = {
        val sentences = tokenizer.tokenize(textRange, restoreCase = true)
        sentences
      }
      val combinedSentences = preservedSentences.zip(restoredSentences).map { case (preservedSentence, restoredSentence) =>
        val percentNotLower = getPercentNotLower(preservedSentence)

        if (percentNotLower >= cutoff) restoredSentence
        else preservedSentence
      }

      new DocumentByWord(None, textRange, combinedSentences)
    }
    val children = document.getChildren.flatMap { textRange =>
      textRange match {
        case wordDocument: WordDocument =>
          // Use the processorsWord here instead of something else like the original, raw text.
          Seq(wordDocument.preSeparator, TextRange(wordDocument.processorsWord), wordDocument.postSeparator)
        case _ => Some(textRange)
      }
    }

    TextRanges(children)
  }
}

object CasePreprocessor {
  val defaultCutoff = 67.5f

  lazy val tokenizer = DocumentByWord.tokenizer
}
