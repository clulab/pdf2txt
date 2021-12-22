package org.clulab.pdf2txt.apps.preprocessor

import org.clulab.pdf2txt.common.utils.Closer.AutoCloser
import org.clulab.pdf2txt.common.utils.StringUtils._
import org.clulab.pdf2txt.common.utils.{Sourcer, StringUtils}
import org.clulab.pdf2txt.document.physical.DocumentByChar

class UnicodePreprocessor(unicodeOptions: UnicodeOptions) extends Preprocessor {

  def preprocess(rawText: String, range: Range, stringBuilder: StringBuilder): Unit = {
    val document = DocumentByChar(rawText)

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

object UnicodePreprocessor {
  lazy val unicodeMap: Map[Char, String] = mkUnicodeMap("/org/clulab/pdf2txt/unicode_to_ascii.tsv")
  lazy val accentSet: Set[Char] = mkAccentSet("/org/clulab/processors/bionlp/accented_characters.tsv")
  val defaultUnicodeOptions = UnicodeOptions(unknownToSpace = true, knownToSpace = false, keepKnownAccent = false)

  def apply: UnicodePreprocessor = apply(defaultUnicodeOptions)
  def apply(unicodeOptions: UnicodeOptions): UnicodePreprocessor = new UnicodePreprocessor(defaultUnicodeOptions)

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
