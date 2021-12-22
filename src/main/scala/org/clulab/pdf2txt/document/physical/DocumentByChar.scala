package org.clulab.pdf2txt.document.physical

import org.clulab.pdf2txt.common.utils.TextRange
import org.clulab.pdf2txt.document.Document

class DocumentByChar(textRange: TextRange) extends Document(textRange) {

  def byChar: Iterator[Char] = textRange.iterator
}
