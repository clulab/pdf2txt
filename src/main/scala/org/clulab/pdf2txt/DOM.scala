package org.clulab.pdf2txt

import org.clulab.pdf2txt.common.utils.StringUtils._

class TextRange(rawText: String, range: Range) {
  def getRawText: String = rawText.substring(range)

  def isEmpty: Boolean = range.isEmpty

  def setText(newText: String): Unit = _

  def getCookedText: String = rawText

  def addCookedText(stringBuilder: StringBuilder): Unit =
      stringBuilder += getCookedText

  def start: Int = range.start

  def end: Int = range.end
}

class Document(rawText: String, range: Range) extends TextRange(rawText, range) {

  def this(rawText: String) = this(rawText, Range(0, rawText.length))

  def parse(): Seq[Paragraph] = {
    // 3. Add implicit end-of-sentence punctuation. That is, the generated text
    // often include 2+ new lines to indicate different paragraphs. For example:
    val separators = "\\n{2,}".r.findAllMatchIn(rawText).map { separator =>
      new Separator(rawText, Range(separator.start, separator.end))
    }.toSeq
    val preContents =
        if (separators.isEmpty) Seq.empty // Save it for post, if necessary.
        else Seq(new Content(rawText, Range(0, separators.head.start)))
    val interContents = separators.sliding(2).map { case Seq (left, right) =>
      new Content(rawText, Range(left.end, right.start))
    }
    val (postContents, postSeparators) =
        if (separators.isEmpty)
          if (this.isEmpty) (Seq.empty, Seq.empty)
          // We always end with a Separator, so extra Content requires another Separator.
          else (new Content(rawText, range), new Separator(rawText, Range(range.end, range.end)))
        else



//    val paragraphs = contents.zip(separators).map { case (content, separator) =>
//      Paragraph(rawText, content, separator)
//    }

//    paragraphs
    null
  }

  val paragraphs: Seq[Paragraph] = parse()

  override def getCookedText: String = paragraphs
      .foldLeft(new StringBuilder) { (stringBuilder, paragraph) =>
        (stringBuilder += paragraph.getCookedText): StringBuilder
      }
      .toString
}

class Content(rawText: String, range: Range) extends TextRange(rawText, range) {
}

class Separator(rawText: String, range: Range) extends TextRange(rawText, range) {
}

case class Paragraph(rawText: String, content: Content, separator: Separator)
    extends TextRange(rawText, Range(content.start, separator.end)) {

  override def getCookedText: String = rawText
}
