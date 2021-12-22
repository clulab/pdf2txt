package org.clulab.pdf2txt.document

import org.clulab.pdf2txt.common.utils.StringUtils._
import org.clulab.pdf2txt.common.utils.TextRange

abstract class Document(rawText: String, range: Range) extends TextRange(rawText, range) {
}

object Document {
  val noContentsOrSeparators: (Seq[Nothing], Seq[Nothing]) = (Seq.empty, Seq.empty)
}

trait DocumentConstructor {
  def apply(rawText: String): Document = apply(rawText, rawText.range)
  def apply(rawText: String, range: Range): Document
}
