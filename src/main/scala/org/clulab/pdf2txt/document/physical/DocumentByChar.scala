package org.clulab.pdf2txt.document.physical

import org.clulab.pdf2txt.common.utils.Closer.AutoCloser
import org.clulab.pdf2txt.common.utils.StringUtils._
import org.clulab.pdf2txt.common.utils.{Sourcer, StringUtils}
import org.clulab.pdf2txt.document.{Document, DocumentConstructor}

class DocumentByChar protected(rawText: String, range: Range) extends Document(rawText, range) {

  def this(rawText: String) = this(rawText, Range(0, rawText.length))

  override def addCookedText(stringBuilder: StringBuilder): Unit = {
    rawText.substring(range).foreach { char =>
      // Most of the time this will be the singleton None, so there is no object creation overhead.
      val cookedOpt = DocumentByChar.unicodeMap.get(char)

      if (cookedOpt.isEmpty) stringBuilder.append(char) // Avoid conversion to string.
      else stringBuilder.append(cookedOpt.get)
    }
  }
}

object DocumentByChar extends DocumentConstructor {
  val unicodeMap: Map[Char, String] = mkUnicodeMap("/org/clulab/pdf2txt/unicode_to_ascii.tsv")
  val accentMap: Map[Char, String] = mkAccentMap()
  
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

  def apply(rawText: String): DocumentByChar = new DocumentByChar(rawText)
}
