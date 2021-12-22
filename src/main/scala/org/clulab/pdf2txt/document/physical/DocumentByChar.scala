package org.clulab.pdf2txt.document.physical

import org.clulab.pdf2txt.document.{Document, DocumentConstructor}

class DocumentByChar protected(rawText: String, range: Range) extends Document(rawText, range) {

  class CharIterator() extends Iterator[Char] {
    val rangeIterator = range.iterator

    override def hasNext: Boolean = rangeIterator.hasNext

    override def next(): Char = rawText(rangeIterator.next)
  }

  def byChar: Iterator[Char] = new CharIterator()
}

object DocumentByChar extends DocumentConstructor {
  def apply(rawText: String): DocumentByChar = new DocumentByChar(rawText, rawText.range)
  def apply(rawText: String, range: Range): DocumentByChar = new DocumentByChar(rawText, range)
}
