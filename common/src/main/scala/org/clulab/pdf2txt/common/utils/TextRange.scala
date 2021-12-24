package org.clulab.pdf2txt.common.utils

import org.clulab.pdf2txt.common.utils.StringUtils._

import scala.collection.mutable
import scala.util.matching.Regex

class TextRange(val text: String, val range: Range) extends Iterable[Char] {

  def this(textRange: TextRange) = this(textRange.text, textRange.range)

  override def toString: String = text.substring(range)

  override def isEmpty: Boolean = range.isEmpty

  def start: Int = range.start

  def end: Int = range.end

  def iterator: Iterator[Char] = new CharIterator(text, range)

  def findAll(regex: Regex): Seq[TextRange] = {
    regex.findAllMatchIn(toString).map { found =>
      TextRange(text, Range(range.start + found.start, range.start + found.end))
    }.toSeq
  }

  def emptyStart: TextRange = emptyRange(start)

  def emptyEnd: TextRange = emptyRange(end)

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
      val interTextRanges = PairIterator(textRanges).map { case (leftTextRange, rightTextRange) =>
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

  def before(pos: Int): TextRange = subRange(start, pos)

  def after(pos: Int): TextRange = subRange(pos, end)

  def all: TextRange = this

  def length: Int = range.length

  def withoutLast: TextRange = if (isEmpty) this else subRange(start, end - 1)

  def withoutHead: TextRange = if (isEmpty) this else subRange(1, end)
}

object TextRange {

  def apply(text: String): TextRange = apply(text, text.range)

  def apply(text: String, range: Range): TextRange = new TextRange(text, range)

  def apply(prev: TextRange, next: TextRange): TextRange = {
    assert(prev.text eq next.text)
    assert(prev.end == next.start)

    TextRange(prev.text, Range(prev.start, next.end))
  }
}

class TextRanges() extends mutable.ArrayBuffer[TextRange]() {

  def +=(textRangeOpt: Option[TextRange]): TextRanges = this ++= textRangeOpt.toSeq
}
