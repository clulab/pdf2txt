package org.clulab.pdf2txt.document.logical

import org.clulab.pdf2txt.common.utils.StringUtils._
import org.clulab.pdf2txt.common.utils.{PairIterator, StringUtils, TextRange}
import org.clulab.pdf2txt.document.physical.CharDocument
import org.clulab.pdf2txt.document.{Document, Separator}

import scala.util.matching.Regex

// multiple paragraphs comprising entire document, contents are paragraphs
case class DocumentByParagraph(override val parentOpt: Option[Document], textRange: TextRange) extends Document(parentOpt, textRange) {
  override val (preSeparatorOpt, contents, postSeparatorOpt) = {
    val found = textRange.findAll(DocumentByParagraph.separatorRegex).toArray
    val contents: Array[TextRange] = textRange.removeAll(found).toArray
    val preSeparator =
        if (contents.isEmpty) textRange.all
        else textRange.before(contents.head.start)
    val interSeparators = PairIterator(contents).map { case (prev, next) =>
      textRange.subRange(prev.end, next.start)
    }.toArray
    val postParagraphSeparator =
        if (contents.isEmpty) textRange.emptyEnd
        else textRange.after(contents.last.end)
    val postSeparator = textRange.emptyEnd // it is used by the paragraph
    val paragraphDocuments = contents.indices.map { index =>
        val contentTextRange = contents(index)
        val separatorTextRange = interSeparators.lift(index).getOrElse(postParagraphSeparator)

      ParagraphDocument(Some(this), contentTextRange, separatorTextRange)
    }

    (
      newSeparatorOpt(preSeparator),
      paragraphDocuments,
      newSeparatorOpt(postSeparator)
    )
  }

  // just do contents, but make sure iterators over paragraph documents
  def byParagraph: Iterator[ParagraphDocument] = contents.iterator
}

object DocumentByParagraph {
  val separatorRegex: Regex = {
    val result = StringUtils.PARAGRAPH_BREAK_STRINGS.mkString("(", "|", "){2,}").r
    result
  }
}

case class ParagraphDocument(override val parentOpt: Option[Document], contentTextRange: TextRange, separatorTextRange: TextRange)
    extends Document(parentOpt, TextRange(contentTextRange, separatorTextRange)) {
  override val postSeparatorOpt = Some(new Separator(Some(this), separatorTextRange))
  val charDocument = CharDocument(Some(this), contentTextRange)
  override val contents = Seq(charDocument)

  def hasEndOfSentence: Boolean = {
    val reverseText = charDocument.toString.withoutWhitespace.reverse

    ParagraphDocument.reverseSentenceBreakStrings.exists { reverseSentenceBreak =>
      reverseText.startsWith(reverseSentenceBreak)
    }
  }

  def hasText: Boolean = {
    !charDocument.forall { char =>
      StringUtils.WHITESPACE_CHARS.contains(char)
    }
  }
}

object ParagraphDocument {
  val reverseSentenceBreakStrings: Array[String] = StringUtils.SENTENCE_BREAK_STRINGS.map(_.reverse)
}
