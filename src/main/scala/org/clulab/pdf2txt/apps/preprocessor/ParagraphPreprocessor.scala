package org.clulab.pdf2txt.apps.preprocessor

import org.clulab.pdf2txt.common.utils.StringUtils._
import org.clulab.pdf2txt.document.logical.DocumentByParagraph

class ParagraphPreprocessor(rawText: String, range: Range) extends Preprocessor(rawText, range) {
  val document = DocumentByParagraph(rawText)

  def addCookedText(stringBuilder: StringBuilder): Unit = {
    document.byParagraph.foreach { paragraph =>
      paragraph.addCookedText(stringBuilder)
    }
  }
}

object ParagraphPreprocessor extends PreprocessorConstructor {
  override def apply(rawText: String): Preprocessor = apply(rawText, rawText.range)
  override def apply(rawText: String, range: Range): Preprocessor = new ParagraphPreprocessor(rawText, range)
}
