package org.clulab.pdf2txt.preprocessor

import org.clulab.pdf2txt.common.utils.TextRange
import org.clulab.pdf2txt.document.logical.DocumentByParagraph

import scala.collection.mutable

class ParagraphPreprocessor extends Preprocessor {

  def preprocess(textRange: TextRange): Seq[TextRange] = {
    val document = new DocumentByParagraph(None, textRange)
    val textRanges = new mutable.ArrayBuffer[TextRange]()

    textRanges += document.preSeparator
    document.byParagraph.foreach { paragraph =>
      textRanges += paragraph.content
      if (paragraph.content.hasText && !paragraph.content.hasEndOfSentence)
        textRanges += TextRange(" .")
      textRanges += paragraph.separator
    }
    textRanges += document.postSeparator
    textRanges
  }
}
