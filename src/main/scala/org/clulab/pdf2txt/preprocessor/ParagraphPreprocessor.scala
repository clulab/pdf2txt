package org.clulab.pdf2txt.preprocessor

import org.clulab.pdf2txt.common.utils.{Preprocessor, TextRange, TextRanges}
import org.clulab.pdf2txt.document.logical.DocumentByParagraph

class ParagraphPreprocessor extends Preprocessor {

  def preprocess(textRange: TextRange): TextRanges = {
    val document = DocumentByParagraph(textRange)
    val textRanges = new TextRanges()

    textRanges += document.preSeparator
    document.contents.foreach { paragraph =>
      textRanges += paragraph.preSeparator
      textRanges ++= paragraph.contents
      if (paragraph.hasText && !paragraph.hasEndOfSentence)
        textRanges += TextRange(" .")
      textRanges += paragraph.postSeparator
    }
    textRanges += document.postSeparator
    textRanges
  }
}
