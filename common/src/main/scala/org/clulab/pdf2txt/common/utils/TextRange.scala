package org.clulab.pdf2txt.common.utils

import org.clulab.pdf2txt.common.utils.StringUtils._

import scala.collection.mutable
import scala.util.matching.Regex

class TextRange(val text: String, val range: Range) extends IndexedSeq[Char] {
  // TODO: Add equality with another text range

  def this(textRange: TextRange) = this(textRange.text, textRange.range)

  override def toString: String = text.substring(range)

  override def isEmpty: Boolean = range.isEmpty

  def start: Int = range.start

  def end: Int = range.end

  def findAll(regex: Regex): Seq[TextRange] = {
    regex.findAllMatchIn(toString).map { found =>
      subRange(found.start, found.end) + range.start
    }.toSeq
  }

  def emptyStart: TextRange = emptyRange(start)

  def emptyEnd: TextRange = emptyRange(end)

  def emptyRange(pos: Int): TextRange = TextRange(text, Range(pos, pos))

  def subRange(range: Range): TextRange = TextRange(text, range)

  def subRange(pos: Int): TextRange = subRange(Range(pos, pos + 1))

  def subRange(start: Int, end: Int): TextRange = TextRange(text, Range(start, end))

  def removeAll(textRanges: IndexedSeq[TextRange]): IndexedSeq[TextRange] = {
    // The ranges should be pre-sorted and be based on the same text.
    // They should also be inside this text range and not themselves be empty.
    if (textRanges.isEmpty) IndexedSeq(this)
    else {
      val preTextRanges =
          if (start < textRanges.head.start) IndexedSeq(subRange(start, textRanges.head.start))
          else IndexedSeq.empty
      val interTextRanges = PairIndexedSeq(textRanges).map { case (leftTextRange, rightTextRange) =>
        subRange(leftTextRange.end, rightTextRange.start)
      }
      val postTextRanges =
          if (textRanges.last.end < end) IndexedSeq(subRange(textRanges.last.end, end))
          else IndexedSeq.empty

      preTextRanges ++ interTextRanges ++ postTextRanges
    }
  }

  def matches(string: String): Boolean = {
    val offset = start

    length == string.length &&
        string.indices.forall { index => string(index) == text(offset + index) }
  }

  def andBefore(textRange: TextRange): TextRange = before(textRange.end)

  def before(pos: Int): TextRange = subRange(start, pos)

  def before(textRange: TextRange): TextRange = before(textRange.start)

  def after(pos: Int): TextRange = subRange(pos, end)

  def after(textRange: TextRange): TextRange = after(textRange.end)

  def andAfter(textRange: TextRange): TextRange = after(textRange.start)

  def all: TextRange = this

  def length: Int = range.length

  // Is dropRight(1)
  // dropLast
  def withoutLast: TextRange = if (isEmpty) this else subRange(start, end - 1)

  // take()
  // drop()
  def withoutHead: TextRange = if (isEmpty) this else subRange(1, end)

  def +(pos: Int): TextRange = subRange(pos + start, pos + end)

  override def apply(index: Int): Char =
      if (0 <= index && index < length) text(start + index)
      else throw new IndexOutOfBoundsException(s"$index is not within interval [0, $length)!")
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

  override def toString: String = foldLeft(new StringBuilder()) { case (stringBuilder, textRange) =>
    stringBuilder ++= textRange.toString
  }.toString
}

object TextRanges {

  def apply(): TextRanges = new TextRanges()

  def apply(textRange: TextRange): TextRanges = new TextRanges() += textRange

  def test: Unit = {
    val textRange = TextRange("hello")

    textRange.foreach { char =>
      println(char)
    }
  }
}
