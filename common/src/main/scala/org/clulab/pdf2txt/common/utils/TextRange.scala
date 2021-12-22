package org.clulab.pdf2txt.common.utils

import org.clulab.pdf2txt.common.utils.StringUtils.{range, _}

import scala.util.matching.Regex

case class TextRange(text: String, range: Range) {

  override def toString: String = text.substring(range)

  def isEmpty: Boolean = range.isEmpty

  def nonEmpty: Boolean = !isEmpty

  def start: Int = range.start

  def end: Int = range.end

  def iterator: Iterator[Char] = new CharIterator(text, range)

  def findAll(regex: Regex): Seq[TextRange] = {
    regex.findAllMatchIn(toString).map { found =>
      TextRange(text, Range(range.start + found.start, range.start + found.end))
    }.toSeq
  }

  def startRange: TextRange = emptyRange(start)

  def endRange: TextRange = emptyRange(end)

  def emptyRange(pos: Int): TextRange = TextRange(text, Range(pos, pos))

  def subRange(range: Range): TextRange = TextRange(text, range)

  def subRange(start: Int, end: Int): TextRange = TextRange(text, Range(start, end))

  def removeAll(textRanges: Seq[TextRange]): Seq[TextRange] = {
    // The ranges should be pre-sorted and be based on the same text.
    // They should also be inside this text range and not themselves be empty.
    if (textRanges.isEmpty) Seq(this)
    else {
      val preTextRanges =
          if (start < textRanges.head.start) Seq.empty
          else Seq(subRange(start, textRanges.head.start))
      val interTextRanges = textRanges.sliding(2).map { case Seq(leftTextRange, rightTextRange) =>
        subRange(leftTextRange.end, rightTextRange.start)
      }.toSeq
      val postTextRanges =
          if (textRanges.last.end < end) Seq.empty
          else Seq(subRange(textRanges.last.end, end))

      preTextRanges ++ interTextRanges ++ postTextRanges
    }
  }

  def matches(string: String): Boolean = {
    val offset = start

    text.length == string.length &&
        string.indices.forall { index => string(index) == text(offset + index) }
  }
}

object TextRange {

  def apply(text: String): TextRange = TextRange(text, text.range)
}
