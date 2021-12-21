package org.clulab.pdf2txt.document

import org.clulab.pdf2txt.LanguageModel
import org.clulab.pdf2txt.common.utils.{CookedText, TextRange}

abstract class Document(rawText: String, range: Range) extends TextRange(rawText, range) with CookedText {
  val noContentsOrSeparators = Document.noContentsOrSeparators
}

object Document {
  val noContentsOrSeparators: (Seq[Nothing], Seq[Nothing]) = (Seq.empty, Seq.empty)
  val languageModel = new LanguageModel()
}

trait DocumentConstructor {
  def apply(rawText: String): Document
}
