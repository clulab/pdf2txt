package org.clulab.pdf2txt.document.physical

import org.clulab.pdf2txt.common.utils.StringUtils._
import org.clulab.pdf2txt.common.utils.{StringUtils, TextRange}
import org.clulab.pdf2txt.document.{Document, DocumentConstructor}

import scala.util.matching.Regex

// Make sure that words split between lines are joined back up.
class DocumentByLine(rawText: String, range: Range) extends Document(rawText, range) {

  def newContent(range: Range): LineContent = new LineContent(rawText, range)

  def newSeparator(range: Range): LineSeparator = new LineSeparator(rawText, range)

  def newContents(range: Range): Seq[LineContent] = Seq(newContent(range))

  def newPostSeparators(): Seq[LineSeparator] = Seq(newSeparator(Range(range.end, range.end)))

  def parse(): Seq[Line] = {
    val separators = DocumentByLine.separatorRegex.findAllMatchIn(rawText.substring(range)).map { separator =>
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
          if (this.isEmpty) Document.noContentsOrSeparators
          // Handle the entire content and close with a Separator.
          else (newContents(range), newPostSeparators())
        else
          if (separators.last.end >= range.end) Document.noContentsOrSeparators
          // Handle any content trailing the last Separator.
          else (newContents(Range(separators.last.end, range.end)), newPostSeparators())
    val allContents = preContents ++ interContents ++ postContents
    val allSeparators = separators ++ postSeparators

    assert(allContents.length == separators.length)

    val paragraphs = allContents.zip(allSeparators).map { case (content, separator) =>
      Line(rawText, content, separator)
    }

    paragraphs
  }

  val lines: Seq[Line] = parse()

  def addCookedText(stringBuilder: StringBuilder): Unit = {
    lines.foldRight(None: Option[Line]) { (currLine, nextLineOpt) =>
      currLine.addCookedText(stringBuilder, nextLineOpt)
      Some(currLine)
    }
  }
}

object DocumentByLine extends DocumentConstructor {
  val separatorRegex: Regex = StringUtils.LINE_BREAK_STRINGS.map(_ + "{1,}").mkString("(", "|", ")").r

  override def apply(rawText: String): DocumentByLine = apply(rawText, rawText.range)
  override def apply(rawText: String, range: Range): DocumentByLine = new DocumentByLine(rawText, range)
}

class LineContent(rawText: String, range: Range) extends TextRange(rawText, range) {

  def getFirstWordOpt(text: String): Option[String] = None

  def getLastWordOpt(text: String): Option[String] = None

  def addCookedText(stringBuilder: StringBuilder, nextContentOpt: Option[LineContent]): Boolean = {

    def addRawText(): Boolean = {
      stringBuilder ++ rawText
      false
    }

    def addCooked(): Boolean = {
      true
    }

    if (nextContentOpt.isEmpty)
      // There is no next line, so just add the current one.
      addRawText()
    else {
      val endsWithHyphen = rawText.nonEmpty && rawText(range.last - 1) == StringUtils.HYPHEN // TODO

      if (!endsWithHyphen)
        addRawText()
      else {
        val nextContent = nextContentOpt.get
        val startsWithChar = nextContent.nonEmpty // && rawText()
true
      }
    }

//    val prevWordOpt = getLastWordOpt(rawText.substring(range))
//    val nextWordOpt = nextContentOpt.map { text => getFirstWordOpt(text.rawText.substring(range)) }
//    stringBuilder ++ rawText
//    if (!isEmpty)
      // ... and the line on top does not end with end-of-sentence punctuation, add " ." to it.
//      stringBuilder ++ " ." // Add this to the last sentence?
  }
}

class LineSeparator(rawText: String, range: Range) extends TextRange(rawText, range) {
}

case class Line protected (rawText: String, content: LineContent, separator: LineSeparator)
  extends TextRange(rawText, Range(content.start, separator.end)) {

  def addCookedText(stringBuilder: StringBuilder, nextLineOpt: Option[Line]): Unit = {
    // If the lines should be combined, addCookedText returns true and then
    // the separator should be skipped so that the lines are joined.
    if (!content.addCookedText(stringBuilder, nextLineOpt.map(_.content)))
      separator.addText(stringBuilder)
  }
}
