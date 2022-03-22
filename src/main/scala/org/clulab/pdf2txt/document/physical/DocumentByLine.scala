package org.clulab.pdf2txt.document.physical

import org.clulab.pdf2txt.common.utils.{StringUtils, TextRange}
import org.clulab.pdf2txt.document.{Document, Separator}

import scala.util.matching.Regex

class DocumentByLine(parentOpt: Option[Document], textRange: TextRange) extends Document(parentOpt, textRange) {
  val lineDocuments: Seq[LineDocument] = {
    val splits = textRange.findAll(DocumentByLine.separatorRegex)
    val separatedPages = splits.indices.map { index =>
      val contentStart =
        if (index == 0) textRange.start
        else splits(index - 1).end
      val contentEnd = splits(index).start

      new LineDocument(parentOpt, textRange.subRange(contentStart, contentEnd), splits(index))
    }
    val unseparatedPageOpt = {
      val contentStart =
        if (splits.isEmpty) textRange.start
        else splits.last.end
      val contentEnd = textRange.end
      val contentTextRange = textRange.subRange(contentStart, contentEnd)

      if (contentTextRange.isEmpty) None
      else Some(new LineDocument(parentOpt, contentTextRange, textRange.emptyEnd))
    }

    separatedPages ++ unseparatedPageOpt
  }
  override val contents: Seq[LineDocument] = lineDocuments
}

object DocumentByLine {
  val separatorRegex: Regex = StringUtils.PARAGRAPH_BREAK_STRINGS.mkString("(", "|", ")").r

  def apply(text: String): DocumentByLine = apply(TextRange(text))
  def apply(textRange: TextRange): DocumentByLine = new DocumentByLine(None, textRange)
}

class LineDocument(parentOpt: Option[Document], contentTextRange: TextRange, separatorTextRange: TextRange)
    extends Document(parentOpt, TextRange(contentTextRange, separatorTextRange)) {
  override val postSeparator: Separator = newSeparator(separatorTextRange)
  val charDocument: CharDocument = new CharDocument(Some(this), contentTextRange)
  override val contents: Seq[CharDocument] = Array(charDocument)
}
