package org.clulab.pdf2txt.document.logical

import org.clulab.pdf2txt.common.utils.StringUtils._
import org.clulab.pdf2txt.common.utils.{CookedText, StringUtils, TextRange}
import org.clulab.pdf2txt.document.{Document}

import scala.util.matching.Regex

// Make sure that each paragraph ends with content that is terminated like a sentence should be.
class DocumentByParagraph(textRange: TextRange) extends Document(textRange) {
  val paragraphTextRanges = textRange.removeAll(textRange.findAll(DocumentByParagraph.separatorRegex)).toArray
  val preSeparator =
      if (paragraphTextRanges.isEmpty) textRange // all of it
      else textRange.subRange(textRange.start, paragraphTextRanges.head.start)
  val interSeparators = paragraphTextRanges.sliding(2).map { case Array(prev, next) =>
    textRange.subRange(prev.end, next.start)
  }.toArray
  val postSeparator =
      if (paragraphTextRanges.isEmpty) textRange.endRange // none of it
      else textRange.subRange(paragraphTextRanges.last.end, textRange.end)
  val paragraphs = paragraphTextRanges.indices.map { index =>
    val paragraphContent = new ParagraphContent(paragraphTextRanges(index))
    val paragraphSeparator = new ParagraphSeparator(interSeparators.lift(index).getOrElse(postSeparator))

    Paragraph(paragraphContent, paragraphSeparator)
  }

  def byParagraph: Iterator[Paragraph] = paragraphs.iterator
}

object DocumentByParagraph {
  val separatorRegex: Regex = StringUtils.PARAGRAPH_BREAK_STRINGS.map(_ + "{2,}").mkString("(", "|", ")").r
}

class ParagraphContent(val textRange: TextRange) {

  def hasEndOfSentence: Boolean = {
    val reverseText = textRange.toString.withoutWhitespace.reverse

    ParagraphContent.reverseSentenceBreakStrings.exists { reverseSentenceBreak =>
      reverseText.startsWith(reverseSentenceBreak)
    }
  }

  def hasText: Boolean = true
}

object ParagraphContent {
  val reverseSentenceBreakStrings: Array[String] = StringUtils.SENTENCE_BREAK_STRINGS.map(_.reverse)
}

class ParagraphSeparator(val textRange: TextRange)

case class Paragraph(content: ParagraphContent, separator: ParagraphSeparator)
