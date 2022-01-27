package org.clulab.pdf2txt.document.logical

import org.clulab.pdf2txt.common.utils.StringUtils._
import org.clulab.pdf2txt.common.utils.{DoubleIndexedSeq, StringUtils, TextRange}
import org.clulab.pdf2txt.document.physical.CharDocument
import org.clulab.pdf2txt.document.{Document, Separator}

import scala.util.matching.Regex

// multiple paragraphs comprising entire document, contents are paragraphs
class DocumentByParagraph(parentOpt: Option[Document], textRange: TextRange) extends Document(parentOpt, textRange) {
  override val (preSeparator, contents, postSeparator) = {
    val found = textRange.findAll(DocumentByParagraph.separatorRegex).toVector
    val contents = textRange.removeAll(found)
    val preSeparator =
        if (contents.isEmpty) all
        else before(contents.head.start)
    val interSeparators = DoubleIndexedSeq(contents).map { case (prev, next) =>
      subRange(prev.end, next.start)
    }.toArray
    val postParagraphSeparator =
        if (contents.isEmpty) emptyEnd
        else after(contents.last.end)
    val postSeparator = emptyEnd // Tt is used by the paragraph.
    val paragraphDocuments = contents.indices.map { index =>
        val contentTextRange = contents(index)
        val separatorTextRange = interSeparators.lift(index).getOrElse(postParagraphSeparator)

      new ParagraphDocument(Some(this), contentTextRange, separatorTextRange)
    }

    (newSeparator(preSeparator), paragraphDocuments, newSeparator(postSeparator))
  }
}

object DocumentByParagraph {
  val separatorRegex: Regex = StringUtils.PARAGRAPH_BREAK_STRINGS.mkString("(", "|", "){2,}").r

  def apply(text: String): DocumentByParagraph = apply(TextRange(text))
  def apply(textRange: TextRange): DocumentByParagraph = new DocumentByParagraph(None, textRange)
}

class ParagraphDocument(parentOpt: Option[Document], contentTextRange: TextRange, separatorTextRange: TextRange)
    extends Document(parentOpt, TextRange(contentTextRange, separatorTextRange)) {
  override val postSeparator: Separator = newSeparator(separatorTextRange)
  val charDocument: CharDocument = new CharDocument(Some(this), contentTextRange)
  override val contents: Seq[CharDocument] = Array(charDocument)

  def hasEndOfSentence: Boolean = {
    val reverseText = charDocument.toString.withoutWhitespace.reverse

    ParagraphDocument.reverseSentenceBreakStrings.exists { reverseSentenceBreak =>
      reverseText.startsWith(reverseSentenceBreak)
    }
  }

  def hasText: Boolean = charDocument.exists(!StringUtils.WHITESPACE_CHARS.contains(_))
}

object ParagraphDocument {
  val reverseSentenceBreakStrings: Array[String] = StringUtils.SENTENCE_BREAK_STRINGS.map(_.reverse)
}
