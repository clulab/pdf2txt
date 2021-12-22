package org.clulab.pdf2txt.preprocessor

import org.clulab.pdf2txt.common.utils.Closer.AutoCloser
import org.clulab.pdf2txt.common.utils.StringUtils._
import org.clulab.pdf2txt.common.utils.{Sourcer, StringUtils, TextRange}
import org.clulab.pdf2txt.document.physical.DocumentByChar

class UnicodePreprocessor(unicodeOptions: UnicodeOptions = UnicodePreprocessor.defaultUnicodeOptions) extends Preprocessor {

  // Return a new text range with all the characters in it.
  // Otherwise, there might be a text range for every character.
  def preprocess(textRange: TextRange): Seq[TextRange] = {
    val SPACE = ' '
    val document = new DocumentByChar(textRange)
    val stringBuilder = new StringBuilder()

    document.byChar.foreach { char =>
      if (char < 0x80) stringBuilder + char
      else {
        val asciiOpt = UnicodePreprocessor.unicodeMap.get(char)
        val known = asciiOpt.isDefined

        if (known)
          if (unicodeOptions.knownToSpace) stringBuilder + SPACE
          else
            if (unicodeOptions.keepKnownAccent && UnicodePreprocessor.accentSet(char)) stringBuilder += char
            else stringBuilder ++ asciiOpt.get
        else
          if (unicodeOptions.unknownToSpace) stringBuilder + SPACE
          else stringBuilder + char
      }
    }
    Seq(TextRange(stringBuilder.toString))
  }
}

case class UnicodeOptions(unknownToSpace: Boolean, knownToSpace: Boolean, keepKnownAccent: Boolean)

object UnicodePreprocessor {
  lazy val unicodeMap: Map[Char, String] = mkUnicodeMap("/org/clulab/pdf2txt/unicode_to_ascii.tsv")
  lazy val accentSet: Set[Char] = mkAccentSet("/org/clulab/processors/bionlp/accented_characters.tsv")
  val defaultUnicodeOptions = UnicodeOptions(unknownToSpace = true, knownToSpace = false, keepKnownAccent = false)

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
