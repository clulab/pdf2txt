package org.clulab.pdf2txt.document

import org.clulab.pdf2txt.common.utils.TextRange

abstract class Document(val parentOpt: Option[Document], textRange: TextRange) extends TextRange(textRange) {

  // The children should account for the entire textRange.  If one were to get the text from all
  // children and concatenate it all, it should match the text of the parent.
  def getChildren: Seq[Document] = Seq.empty
}
