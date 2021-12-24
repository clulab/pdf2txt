package org.clulab.pdf2txt.document.physical

import org.clulab.pdf2txt.common.utils.TextRange
import org.clulab.pdf2txt.document.Document

case class DocumentByChar(override val parentOpt: Option[Document], textRange: TextRange) extends Document(parentOpt, textRange) {
  val charDocument = CharDocument(Some(this), textRange)
  override val contents = Array(charDocument)

  def byChar: Iterator[Char] = charDocument.iterator
}

case class CharDocument(override val parentOpt: Option[Document], textRange: TextRange) extends Document(parentOpt, textRange)
