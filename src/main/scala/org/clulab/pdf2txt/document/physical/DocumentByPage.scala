package org.clulab.pdf2txt.document.physical

import org.clulab.pdf2txt.common.utils.{StringUtils, TextRange}
import org.clulab.pdf2txt.document.{Document, Separator}

class DocumentByPage(parentOpt: Option[Document], textRange: TextRange) extends Document(parentOpt, textRange) {
  val pageDocuments: Seq[PageDocument] = {
    val splits = textRange.findAll(DocumentByPage.separatorRegex)
    val separatedPages = splits.indices.map { index =>
      val contentStart =
          if (index == 0) textRange.start
          else splits(index - 1).end
      val contentEnd = splits(index).start

      new PageDocument(parentOpt, textRange.subRange(contentStart, contentEnd), splits(index))
    }
    val unseparatedPageOpt = {
      val contentStart =
          if (splits.isEmpty) textRange.start
          else splits.last.end
      val contentEnd = textRange.end
      val contentTextRange = textRange.subRange(contentStart, contentEnd)

      if (contentTextRange.isEmpty) None
      else Some(new PageDocument(parentOpt, contentTextRange, textRange.emptyEnd))
    }

    separatedPages ++ unseparatedPageOpt
  }
  override val contents: Seq[PageDocument] = pageDocuments
}

object DocumentByPage {
  val separatorRegex = StringUtils.FORM_FEED.toString.r

  def apply(text: String): DocumentByPage = apply(TextRange(text))
  def apply(textRange: TextRange): DocumentByPage = new DocumentByPage(None, textRange)
}

class PageDocument(parentOpt: Option[Document], contentTextRange: TextRange, separatorTextRange: TextRange)
    extends Document(parentOpt, TextRange(contentTextRange, separatorTextRange)) {
  override val postSeparator: Separator = newSeparator(separatorTextRange)
  val charDocument: CharDocument = new CharDocument(Some(this), contentTextRange)
  override val contents: Seq[CharDocument] = Array(charDocument)
}
