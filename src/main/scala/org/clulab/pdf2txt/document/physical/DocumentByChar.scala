package org.clulab.pdf2txt.document.physical

import org.clulab.pdf2txt.common.utils.TextRange
import org.clulab.pdf2txt.document.Document

case class DocumentByChar(override val parentOpt: Option[Document], textRange: TextRange) extends Document(parentOpt, textRange) {

  def byChar: Iterator[Char] = textRange.iterator
}
