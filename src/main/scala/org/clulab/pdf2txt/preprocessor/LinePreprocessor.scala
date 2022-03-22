package org.clulab.pdf2txt.preprocessor

import org.clulab.pdf2txt.common.utils.{TextRange, TextRanges}
import org.clulab.pdf2txt.document.physical.{DocumentByLine, LineDocument}

import scala.annotation.tailrec

class LinePreprocessor extends Preprocessor {

  def preprocess(textRange: TextRange): TextRanges = {
    val document = DocumentByLine(textRange)
    val contents = document.contents
    val lineQualifiers = contents.map(LineQualifier(_))
    val textRanges = new TextRanges()

    @tailrec
    def loop(index: Int): Unit = {
      if (index < contents.length) {
        val top = contents(index)
        textRanges += top

        if (index + 2 < contents.length &&
            lineQualifiers(index).top &&
            lineQualifiers(index + 1).mid &&
            lineQualifiers(index + 2).bot)
          loop(index + 2) // skip mid
        else
          loop(index + 1)
      }
    }

    textRanges += document.preSeparator
    loop(0)
    textRanges += document.postSeparator
    textRanges
  }
}

case class LineQualifier(top: Boolean, mid: Boolean, bot: Boolean)

object LineQualifier {

  def apply(lineDocument: LineDocument): LineQualifier = {
    val string = lineDocument.contents.head.toString.trim
    val top = string.nonEmpty && !string.endsWith(".")
    val mid = string.isEmpty
    val bot = string.nonEmpty && {
      val char = string.head
      char.isLetter && char.isLower
    }
    LineQualifier(top, mid, bot)
  }
}
