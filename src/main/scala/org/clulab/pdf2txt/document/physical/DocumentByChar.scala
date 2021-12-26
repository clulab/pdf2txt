package org.clulab.pdf2txt.document.physical

import org.clulab.pdf2txt.common.utils.TextRange
import org.clulab.pdf2txt.document.Document

class DocumentByChar(parentOpt: Option[Document], textRange: TextRange) extends Document(parentOpt, textRange) {
  val charDocument: CharDocument = new CharDocument(Some(this), textRange)
  override val contents: Seq[CharDocument] = Array(charDocument)
}

object DocumentByChar {
  def apply(text: String): DocumentByChar = apply(TextRange(text))
  def apply(textRange: TextRange): DocumentByChar = new DocumentByChar(None, textRange)
}

class CharDocument(parentOpt: Option[Document], textRange: TextRange) extends Document(parentOpt, textRange)
