package org.clulab.pdf2txt.apps.preprocessor

import org.clulab.pdf2txt.common.utils.Closer.AutoCloser
import org.clulab.pdf2txt.common.utils.StringUtils._
import org.clulab.pdf2txt.common.utils.{Sourcer, StringUtils}
import org.clulab.pdf2txt.document.physical.DocumentByChar


// These should take the text to preprocess, and the range?

class UnicodePreprocessor(rawText: String, range: Range, unicodeOptions: UnicodeOptions) extends Preprocessor(rawText, range) {
  val document = DocumentByChar(rawText)

  def addCookedText(stringBuilder: StringBuilder): Unit = {
    document.byChar.foreach { char =>
      if (char < 0x80) stringBuilder += char
      else {
        val asciiOpt = UnicodePreprocessor.unicodeMap.get(char)
        val known = asciiOpt.isDefined

        if (known)
          if (unicodeOptions.knownToSpace) stringBuilder += ' '
          else
            if (unicodeOptions.keepKnownAccent && UnicodePreprocessor.accentSet(char)) stringBuilder += char
            else stringBuilder ++ asciiOpt.get
        else
          if (unicodeOptions.unknownToSpace) stringBuilder += ' '
          else stringBuilder += char
      }
    }
  }
}

case class UnicodeOptions(unknownToSpace: Boolean, knownToSpace: Boolean, keepKnownAccent: Boolean)

object UnicodePreprocessor extends PreprocessorConstructor {
  lazy val unicodeMap: Map[Char, String] = mkUnicodeMap("/org/clulab/pdf2txt/unicode_to_ascii.tsv")
  lazy val accentSet: Set[Char] = mkAccentSet("/org/clulab/processors/bionlp/accented_characters.tsv")
  val defaultUnicodeOptions = UnicodeOptions(unknownToSpace = true, knownToSpace = false, keepKnownAccent = false)

  override def apply(rawText: String): Preprocessor = apply(rawText, rawText.range)
  override def apply(rawText: String, range: Range): Preprocessor = new UnicodePreprocessor(rawText, range, defaultUnicodeOptions)

  def apply(rawText: String, unicodeOptions: UnicodeOptions): Preprocessor =
      new UnicodePreprocessor(rawText, Range(0, rawText.length), unicodeOptions)

  def mkUnicodeMap(resourceName: String): Map[Char, String] = {
    Sourcer.sourceFromResource(resourceName).autoClose { source =>
      source
          .getLines()
          .filterNot(_.startsWith("#"))
          .map { line =>
            val columns = line.normalizeUnicode.split(StringUtils.TAB)
            require(columns.nonEmpty)
            val key = columns.head.toUnicodeChar
            val value = columns.lift(1).getOrElse("")

            key -> value
          }
          .toMap
    }
  }

  def mkAccentSet(resourceName: String): Set[Char] = {
    Sourcer.sourceFromResource(resourceName).autoClose { source =>
      source
        .getLines()
        .map(_.normalizeUnicode.trim)
        .filter(_.nonEmpty)
        .map { line =>
          line.head
        }
        .toSet
    }
  }
}
