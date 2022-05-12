package org.clulab.pdf2txt.common.utils

import org.clulab.pdf2txt.common.utils.StringUtils._

import scala.collection.mutable
import scala.util.matching.Regex

class TextRange(val text: String, val range: Range) extends IndexedSeq[Char] with CharSequence {

  // Copy constructor
  def this(textRange: TextRange) = this(textRange.text, textRange.range)

  // IndexedSeq[Char] interface
  def length: Int = range.length

  override def apply(index: Int): Char =
    if (0 <= index && index < length) text(start + index)
    else throw new IndexOutOfBoundsException(s"$index is not within interval [0, $length)!")

  // CharSequence interface
  override def charAt(index: Int): Char = this(index)

  override def subSequence(start: Int, end: Int): CharSequence = subRange(this.start + start, this.start + end)

  // Other methods
  override def toString: String = text.substring(range)

  def toString(stringBuilder: StringBuilder): StringBuilder = {
    foreach(stringBuilder.append)
    stringBuilder
  }

  // Getters
  def start: Int = range.start

  def end: Int = range.end

  // Empty
  override def isEmpty: Boolean = range.isEmpty

  def emptyStart: TextRange = emptyRange(start)

  def emptyEnd: TextRange = emptyRange(end)

  def emptyRange(pos: Int): TextRange = TextRange(text, Range(pos, pos))

  def trimmedIsEmpty: Boolean = findFirstTrimmed.isEmpty

  // Slice.  Be careful.  Coordinates start with the text, not existing range.
  def subRange(range: Range): TextRange = TextRange(text, range)

  def subRange(pos: Int): TextRange = subRange(Range(pos, pos + 1))

  def subRange(start: Int, end: Int): TextRange = TextRange(text, Range(start, end))

  // Before
  def andBefore(textRange: TextRange): TextRange = before(textRange.end)

  def before(pos: Int): TextRange = subRange(start, pos)

  def before(textRange: TextRange): TextRange = before(textRange.start)

  // After
  def after(pos: Int): TextRange = subRange(pos, end)

  def after(textRange: TextRange): TextRange = after(textRange.end)

  def andAfter(textRange: TextRange): TextRange = after(textRange.start)

  // First
  def firstChar: Char = text(start) // assuming nonEmpty

  def findFirstTrimmed: TextRange = findFirst { char: Char => char > ' ' }

  def withoutFirst: TextRange = if (isEmpty) this else subRange(start + 1, end)

  // Last
  def lastChar: Char = text(end - 1) // assuming nonEmpty

  def findLastTrimmed: TextRange = findLast { char: Char => char > ' ' }

  def withoutLast: TextRange = if (isEmpty) this else subRange(start, end - 1)

  // Miscellaneous
  def all: TextRange = this

  def +(pos: Int): TextRange = subRange(pos + start, pos + end)

  // Search and replace
  def matches(char: Char): Boolean = length == 1 && head == char

  def matches(string: String): Boolean =
      string.length == length &&
      string.indices.forall { index => string(index) == this(index) }

  def findFirst(f: Char => Boolean): TextRange = range
      .find { pos => f(text(pos)) }
      .map(subRange)
      .getOrElse(emptyEnd)

  def findLast(f: Char => Boolean): TextRange = range.reverse
      .find { pos => f(text(pos)) }
      .map(subRange)
      .getOrElse(emptyStart)

  def findAll(regex: Regex): IndexedSeq[TextRange] = regex
      .findAllMatchIn(this)
      .map { found => subRange(found.start, found.end) + range.start }
      .toIndexedSeq

  def removeAll(textRanges: IndexedSeq[TextRange]): IndexedSeq[TextRange] = {
    // The ranges should be pre-sorted and be based on the same text.
    // They should also be inside this text range and not themselves be empty.
    if (textRanges.isEmpty) IndexedSeq(this)
    else {
      val preTextRanges =
          if (start < textRanges.head.start) IndexedSeq(subRange(start, textRanges.head.start))
          else IndexedSeq.empty
      val interTextRanges = DoubleIndexedSeq(textRanges).map { case (leftTextRange, rightTextRange) =>
        subRange(leftTextRange.end, rightTextRange.start)
      }
      val postTextRanges =
          if (textRanges.last.end < end) IndexedSeq(subRange(textRanges.last.end, end))
          else IndexedSeq.empty

      preTextRanges ++ interTextRanges ++ postTextRanges
    }
  }

  def replaceAll(textRanges: IndexedSeq[TextRange], replacement: TextRange): IndexedSeq[TextRange] = {
    // See removeAll for caveats.
    if (textRanges.isEmpty) IndexedSeq(this)
    else {
      val preTextRanges =
        if (start < textRanges.head.start) IndexedSeq(subRange(start, textRanges.head.start), replacement)
        else IndexedSeq(replacement)
      val interTextRanges = DoubleIndexedSeq(textRanges).flatMap { case (leftTextRange, rightTextRange) =>
        IndexedSeq(subRange(leftTextRange.end, rightTextRange.start), replacement)
      }
      val postTextRanges =
        if (textRanges.last.end < end) IndexedSeq(subRange(textRanges.last.end, end))
        else IndexedSeq.empty

      preTextRanges ++ interTextRanges ++ postTextRanges
    }
  }
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

  override def toString: String = toString(new StringBuilder()).toString

  def toString(stringBuilder: StringBuilder): StringBuilder = {
    foreach(_.toString(stringBuilder))
    stringBuilder
  }
}

object TextRanges {

  def apply(): TextRanges = new TextRanges()

  def apply(textRange: TextRange): TextRanges = new TextRanges() += textRange

  def apply(textRanges: Seq[TextRange]): TextRanges = new TextRanges() ++= textRanges
}
