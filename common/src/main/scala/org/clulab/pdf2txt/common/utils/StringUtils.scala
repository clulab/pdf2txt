package org.clulab.pdf2txt.common.utils

import java.text.Normalizer

object StringUtils {
  val LF = '\n' // line feed
  val CR = '\r' // carriage return

  val HTAB = '\t' // horizontal
  val VTAB = '\u000B' // vertical
  val TAB = HTAB

  val SOFT_SPACE = ' '
  val HARD_SPACE = '\u00A0' // non-breaking
  val SPACE = ' '

  val DASH = '-'

  val SOFT_HYPHEN = '\u00AD'
  val HARD_HYPHEN = '\u2011' // non-breaking
  val HYPHEN = DASH

  val PERIOD = '.'

  val UPRIGHT_EXCLAMATION = '!'
  val INVERTED_EXCLAMATION = '\u00A1'
  val EXCLAMATION = UPRIGHT_EXCLAMATION

  val UPRIGHT_QUESTION = '?'
  val INVERTED_QUESTION = '\u00BF'
  val QUESTION = UPRIGHT_QUESTION

  val SINGLE_QUOTE = '\''
  val DOUBLE_QUOTE = '\"'

  val  LEFT_SINGLE_QUOTE = '\u2018'
  val RIGHT_SINGLE_QUOTE = '\u2019'
  val  LEFT_DOUBLE_QUOTE = '\u201C'
  val RIGHT_DOUBLE_QUOTE = '\u201D'

  val WHITESPACE_CHARS: Array[Char] = Array(SOFT_SPACE, HARD_SPACE, LF, CR, HTAB, VTAB)
  val WHITESPACE_STRINGS: Array[String] = WHITESPACE_CHARS.map(_.toString)
  val PARAGRAPH_BREAK_STRINGS: Array[String] = Array(LF.toString)
  val LINE_BREAK_STRINGS: Array[String] = PARAGRAPH_BREAK_STRINGS
  val SENTENCE_BREAK_CHARS: Array[Char] = Array(PERIOD, UPRIGHT_EXCLAMATION, INVERTED_EXCLAMATION, UPRIGHT_QUESTION, INVERTED_QUESTION)
  val SENTENCE_BREAK_STRINGS: Array[String] = {
    val endPuncts = SENTENCE_BREAK_CHARS.map(_.toString)
    val endQuotes = Array(SINGLE_QUOTE, DOUBLE_QUOTE, RIGHT_SINGLE_QUOTE, RIGHT_DOUBLE_QUOTE).map(_.toString)
    val quotedEndPuncts = endPuncts.flatMap { endPunct =>
      endQuotes.map { endQuote =>
        endPunct + endQuote
      }
    }

    endPuncts ++ quotedEndPuncts
  }
  val WORD_BREAK_CHARS: Array[Char] = Array(SOFT_HYPHEN, HARD_HYPHEN)
  val LETTER_BREAK_CHARS: Array[Char] = Array(SOFT_SPACE, HARD_SPACE)

  def before(string: String, index: Int, all: Boolean, keep: Boolean): String = {
    if (index < 0)
      if (all) string
      else ""
    else string.substring(0, index + (if (keep) 1 else 0))
  }

  def beforeLast(string: String, char: Char, all: Boolean = true, keep: Boolean = false): String =
    before(string, string.lastIndexOf(char), all, keep)

  def beforeFirst(string: String, char: Char, all: Boolean = true, keep: Boolean = false): String =
    before(string, string.indexOf(char), all, keep)

  def after(string: String, index: Int, all: Boolean, keep: Boolean): String = {
    if (index < 0)
      if (all) string
      else ""
    else string.substring(index + (if (keep) 0 else 1))
  }

  def afterLast(string: String, char: Char, all: Boolean = true, keep: Boolean = false): String =
    after(string, string.lastIndexOf(char), all, keep)

  def afterFirst(string: String, char: Char, all: Boolean = true, keep: Boolean = false): String =
    after(string, string.indexOf(char), all, keep)

  def substring(string: String, range: Range): String = string.substring(range.start, range.end)

  def withoutWhitespace(string: String): String = string.filterNot(WHITESPACE_CHARS.contains)

  def normalizeUnicode(string: String): String = Normalizer.normalize(string, Normalizer.Form.NFKC)

  def toUnicodeChar(string: String): Char = Integer.parseInt(string, 16).toChar

  def range(string: String): Range = Range(0, string.length)

  def iterator(string: String): Iterator[Char] = iterator(string, string.range)

  def iterator(string: String, range: Range): Iterator[Char] = new CharIterator(string, range)

  implicit class StringOps(string: String) {

    def substring(range: Range): String = StringUtils.substring(string, range)

    def withoutWhitespace: String = StringUtils.withoutWhitespace(string)

    def beforeLast(char: Char, all: Boolean = true, keep: Boolean = false): String = string

    def normalizeUnicode: String = StringUtils.normalizeUnicode(string)

    def toUnicodeChar: Char = StringUtils.toUnicodeChar(string)

    def range: Range = StringUtils.range(string)

    def toIterator: Iterator[Char] = StringUtils.iterator(string)

    def toIterator(range: Range): Iterator[Char] = StringUtils.iterator(string, range)
  }
}

class CharIterator(string: String, range: Range) extends Iterator[Char] {
  val rangeIterator = range.iterator

  override def hasNext: Boolean = rangeIterator.hasNext

  override def next(): Char = string(rangeIterator.next)
}
