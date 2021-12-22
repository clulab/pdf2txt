package org.clulab.pdf2txt.preprocessor

import org.clulab.pdf2txt.common.utils.TextRange
import org.clulab.pdf2txt.document.logical.DocumentByParagraph

import scala.collection.mutable

class ParagraphPreprocessor extends Preprocessor {

  def preprocess(textRange: TextRange): Seq[TextRange] = {
    val document = new DocumentByParagraph(textRange)
    val textRanges = new mutable.ArrayBuffer[TextRange]()

    textRanges ++ document.preSeparator
    document.byParagraph.foreach { paragraph =>
      textRanges ++ paragraph.content.textRange
      if (!paragraph.content.hasEndOfSentence)
        textRanges ++ TextRange(" .")
      textRanges ++ paragraph.separator.textRange
    }
    textRanges ++ document.postSeparator
    textRanges
  }
}

