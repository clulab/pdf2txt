package org.clulab.pdf2txt.apps.preprocessor

import org.clulab.pdf2txt.common.utils.StringUtils._
import org.clulab.pdf2txt.document.logical.DocumentByParagraph

class ParagraphPreprocessor extends Preprocessor {

  def preprocess(rawText: String, range: Range, stringBuilder: StringBuilder): Unit = {
    val document = DocumentByParagraph(rawText)

    document.byParagraph.foreach { paragraph =>
      paragraph.addCookedText(stringBuilder)
    }
  }
}

object ParagraphPreprocessor {
  def apply: ParagraphPreprocessor = new ParagraphPreprocessor()
}
