package org.clulab.pdf2txt.document.physical

import org.clulab.pdf2txt.common.utils.StringUtils._
import org.clulab.pdf2txt.document.{Document, DocumentConstructor}

class DocumentByChar protected(rawText: String, range: Range) extends Document(rawText, range) {

  def byChar: Iterator[Char] = rawText.toIterator(range)
}

object DocumentByChar extends DocumentConstructor {
  override def apply(rawText: String): DocumentByChar = new DocumentByChar(rawText, rawText.range)
  override def apply(rawText: String, range: Range): DocumentByChar = new DocumentByChar(rawText, range)
}
