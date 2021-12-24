package org.clulab.pdf2txt.preprocessor

import org.clulab.pdf2txt.common.utils.TextRange
import org.clulab.pdf2txt.document.logical.DocumentByParagraph

import scala.collection.mutable

class ParagraphPreprocessor extends Preprocessor {

  def preprocess(textRange: TextRange): Seq[TextRange] = {
    val document = new DocumentByParagraph(None, textRange)
    val textRanges = new mutable.ArrayBuffer[TextRange]()

    textRanges ++= document.preSeparatorOpt.toSeq
    document.contents.foreach { paragraph =>
      textRanges += paragraph
      if (paragraph.hasText && !paragraph.hasEndOfSentence)
        textRanges += TextRange(" .")
    }
    textRanges ++= document.postSeparatorOpt.toSeq
    textRanges
  }
}
