package org.clulab.pdf2txt.document

import org.clulab.pdf2txt.common.utils.StringUtils._
import org.clulab.pdf2txt.common.utils.{StringUtils, TextRange}

trait CookedText {

  def getCookedText: String = addCookedText(new StringBuilder()).toString

  def addCookedText(stringBuilder: StringBuilder): Unit
}

class DocumentByParagraph protected(rawText: String, range: Range) extends TextRange(rawText, range) with CookedText {

  def this(rawText: String) = this(rawText, Range(0, rawText.length))

  def newContent(range: Range): Content = new Content(rawText, range)

  def newSeparator(range: Range): Separator = new Separator(rawText, range)

  def newContents(range: Range): Seq[Content] = Seq(newContent(range))

  def newPostSeparators(): Seq[Separator] = Seq(newSeparator(Range(range.end, range.end)))

  def parse(): Seq[Paragraph] = {
    // 3. Add implicit end-of-sentence punctuation. That is, the generated text
    // often include 2+ new lines to indicate different paragraphs.
    val separators = DocumentByParagraph.separatorRegex.findAllMatchIn(rawText.substring(range)).map { separator =>
      newSeparator(Range(range.start + separator.start, range.start + separator.end))
    }.toSeq
    val preContents =
        if (separators.isEmpty) Seq.empty // Save it for post, if necessary.
        else newContents(Range(range.start, separators.head.start))
    val interContents = separators.sliding(2).map { case Seq (left, right) =>
      newContent(Range(left.end, right.start))
    }
    val (postContents, postSeparators) =
        if (separators.isEmpty)
          if (this.isEmpty) DocumentByParagraph.noContentsOrSeparators
          // Handle the entire content and close with a Separator.
          else (newContents(range), newPostSeparators())
        else
          if (separators.last.end >= range.end) DocumentByParagraph.noContentsOrSeparators
          // Handle any content trailing the last Separator.
          else (newContents(Range(separators.last.end, range.end)), newPostSeparators())
    val allContents = preContents ++ interContents ++ postContents
    val allSeparators = separators ++ postSeparators

    assert(allContents.length == separators.length)

    val paragraphs = allContents.zip(allSeparators).map { case (content, separator) =>
      Paragraph(rawText, content, separator)
    }

    paragraphs
  }

  val paragraphs: Seq[Paragraph] = parse()

  override def addCookedText(stringBuilder: StringBuilder): Unit =
      paragraphs.foreach(_.addCookedText(stringBuilder))
}

object DocumentByParagraph {
  val separatorRegex = StringUtils.PARAGRAPH_BREAK_STRINGS.map(_ + "{2,}").mkString("(", "|", ")").r
  val noContentsOrSeparators = (Seq.empty, Seq.empty)
}

class Content(rawText: String, range: Range) extends TextRange(rawText, range) with CookedText {

  def hasEndOfSentence: Boolean = {
    val reverseText = rawText.withoutWhitespace.reverse

    Content.reverseSentenceBreakStrings.exists { reverseSentenceBreak =>
      reverseText.startsWith(reverseSentenceBreak)
    }
  }

  override def addCookedText(stringBuilder: StringBuilder): Unit = {
    stringBuilder ++ rawText
    if (!isEmpty && !hasEndOfSentence)
      // ... and the line on top does not end with end-of-sentence punctuation, add " ." to it.
      stringBuilder ++ " ." // Add this to the last sentence?
  }
}

object Content {
  val reverseSentenceBreakStrings = StringUtils.SENTENCE_BREAK_STRINGS.map(_.reverse)
}

class Separator(rawText: String, range: Range) extends TextRange(rawText, range) {
}

case class Paragraph protected (rawText: String, content: Content, separator: Separator)
    extends TextRange(rawText, Range(content.start, separator.end)) with CookedText {

  override def addCookedText(stringBuilder: StringBuilder): Unit = {
    content.addCookedText(stringBuilder)
    separator.addText(stringBuilder)
  }
}
