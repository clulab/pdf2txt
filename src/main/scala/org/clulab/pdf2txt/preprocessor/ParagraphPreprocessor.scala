package org.clulab.pdf2txt.preprocessor

import org.clulab.pdf2txt.common.utils.{TextRange, TextRanges}
import org.clulab.pdf2txt.document.logical.DocumentByParagraph

class ParagraphPreprocessor extends Preprocessor {

  def preprocess(textRange: TextRange): TextRanges = {
    val document = new DocumentByParagraph(None, textRange)
    val textRanges = new TextRanges()

    textRanges += document.preSeparatorOpt
    document.contents.foreach { paragraph =>
      textRanges += paragraph.preSeparatorOpt
      textRanges ++= paragraph.contents
      if (paragraph.hasText && !paragraph.hasEndOfSentence)
        textRanges += TextRange(" .")
      textRanges += paragraph.postSeparatorOpt
    }
    textRanges += document.postSeparatorOpt
    textRanges
  }
}
