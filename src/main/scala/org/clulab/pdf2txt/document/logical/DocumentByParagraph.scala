package org.clulab.pdf2txt.document.logical

import org.clulab.pdf2txt.common.utils.StringUtils._
import org.clulab.pdf2txt.common.utils.{PairIterator, StringUtils, TextRange}
import org.clulab.pdf2txt.document.Document

import scala.util.matching.Regex

case class DocumentByParagraph(override val parentOpt: Option[Document], textRange: TextRange) extends Document(parentOpt, textRange) {
  val contents: Array[TextRange] = textRange.removeAll(textRange.findAll(DocumentByParagraph.separatorRegex)).toArray
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
  val paragraphs = contents.indices.map { index =>
    val contentTextRange = contents(index)
    val separatorTextRange = interSeparators.lift(index).getOrElse(postParagraphSeparator)

    Paragraph(Some(this), contentTextRange, separatorTextRange)
  }

  def byParagraph: Iterator[Paragraph] = paragraphs.iterator
}

object DocumentByParagraph {
  val separatorRegex: Regex = StringUtils.PARAGRAPH_BREAK_STRINGS.map(_ + "{2,}").mkString("(", "|", ")").r
}

case class ParagraphContent(override val parentOpt: Option[Document], textRange: TextRange) extends Document(parentOpt, textRange) {

  def hasEndOfSentence: Boolean = {
    val reverseText = textRange.toString.withoutWhitespace.reverse

    ParagraphContent.reverseSentenceBreakStrings.exists { reverseSentenceBreak =>
      reverseText.startsWith(reverseSentenceBreak)
    }
  }

  def hasText: Boolean = {
    !textRange.iterator.forall { char =>
      StringUtils.WHITESPACE_CHARS.contains(char)
    }
  }
}

object ParagraphContent {
  val reverseSentenceBreakStrings: Array[String] = StringUtils.SENTENCE_BREAK_STRINGS.map(_.reverse)
}

case class ParagraphSeparator(override val parentOpt: Option[Document], textRange: TextRange) extends Document(parentOpt, textRange)

case class Paragraph(override val parentOpt: Option[Document], contentTextRange: TextRange, separatorTextRange: TextRange)
    extends Document(parentOpt, TextRange(contentTextRange, separatorTextRange)) {
  val content = ParagraphContent(Some(this), contentTextRange)
  val separator = ParagraphSeparator(Some(this), separatorTextRange)
}
