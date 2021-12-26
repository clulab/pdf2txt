package org.clulab.pdf2txt.document

import org.clulab.pdf2txt.common.utils.TextRange

abstract class Document(val parentOpt: Option[Document], textRange: TextRange) extends TextRange(textRange) {
  val preSeparator: Separator = newSeparator(emptyStart)
  val contents: Seq[Document] = Seq.empty
  val postSeparator: Separator =  newSeparator(emptyEnd)

  // The children should account for the entire textRange.  If one were to get the text from all
  // children and concatenate it all, it should match the text of the parent.
  def getChildren: Seq[TextRange] = preSeparator +: contents :+ postSeparator

  def newSeparator(textRange: TextRange): Separator = new Separator(this, textRange)
}

// The Separator is another document and could have its own separators.  In order to stop the
// recursion, they could be null or Options.
class Separator(parent: Document, textRange: TextRange) extends TextRange(textRange)
