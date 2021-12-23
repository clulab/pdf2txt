package org.clulab.pdf2txt.document.logical

import org.clulab.pdf2txt.common.utils.StringUtils._
import org.clulab.pdf2txt.common.utils.StringUtils
import org.clulab.pdf2txt.common.utils.TextRange
import org.clulab.pdf2txt.document.Document

import scala.util.matching.Regex

class DocumentByParagraph(textRange: TextRange) extends Document(textRange) {
  val contents: Array[TextRange] = textRange.removeAll(textRange.findAll(DocumentByParagraph.separatorRegex)).toArray
  val preSeparator =
      if (contents.isEmpty) textRange.all
      else textRange.before(contents.head.start)
  val interSeparators = contents.sliding(2).map { case Array(prev, next) =>
    textRange.subRange(prev.end, next.start)
  }.toArray
  val postParagraphSeparator =
      if (contents.isEmpty) textRange.emptyEnd
      else textRange.after(contents.last.end)
  val postSeparator = textRange.emptyEnd // it is used by the paragraph
  val paragraphs = contents.indices.map { index =>
    val paragraphContent = ParagraphContent(contents(index))
    val paragraphSeparator = ParagraphSeparator(interSeparators.lift(index).getOrElse(postParagraphSeparator))

    Paragraph(paragraphContent, paragraphSeparator)
  }

  def byParagraph: Iterator[Paragraph] = paragraphs.iterator
}

object DocumentByParagraph {
  val separatorRegex: Regex = StringUtils.PARAGRAPH_BREAK_STRINGS.map(_ + "{2,}").mkString("(", "|", ")").r
}

case class ParagraphContent(textRange: TextRange) {

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

case class ParagraphSeparator(textRange: TextRange)

case class Paragraph(content: ParagraphContent, separator: ParagraphSeparator)
