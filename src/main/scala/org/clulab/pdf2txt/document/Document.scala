package org.clulab.pdf2txt.document

import org.clulab.pdf2txt.common.utils.TextRange

abstract class Document(val parentOpt: Option[Document], textRange: TextRange) extends TextRange(textRange) {
  val preSeparatorOpt: Option[Separator] = Some(new Separator(Some(this), emptyStart))
  val contents: Seq[Document] = Seq.empty
  val postSeparatorOpt: Option[Separator] =  Some(new Separator(Some(this), emptyEnd))

  // The children should account for the entire textRange.  If one were to get the text from all
  // children and concatenate it all, it should match the text of the parent.
  def getChildren: Seq[Document] = {
    preSeparatorOpt.toSeq ++ contents ++ postSeparatorOpt.toSeq
  }
}

// The Separator is another document and could have its own separators.  In order to stop the
// recursion, they could be null or Options.
class Separator(parentOpt: Option[Document], textRange: TextRange) extends Document(parentOpt, textRange) {
  override val preSeparatorOpt: Option[Separator] = None
  override val postSeparatorOpt: Option[Separator] = None
}
