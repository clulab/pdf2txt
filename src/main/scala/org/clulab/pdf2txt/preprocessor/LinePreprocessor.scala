package org.clulab.pdf2txt.preprocessor

import org.clulab.pdf2txt.common.utils.{TextRange, TextRanges}
import org.clulab.pdf2txt.document.physical.{DocumentByLine, LineDocument}

import scala.annotation.tailrec

class LinePreprocessor extends Preprocessor {

  def preprocess(textRange: TextRange): TextRanges = {
    val document = DocumentByLine(textRange)
    val textRanges = new TextRanges()
    val contents = document.contents

    def topQualifies(lineDocument: LineDocument): Boolean = {
      val string = lineDocument.contents.head.toString.trim

      string.nonEmpty && !string.endsWith(".")
    }

    def midQualifies(lineDocument: LineDocument): Boolean = {
      lineDocument.contents.head.toString.trim.isEmpty
    }

    def botQualifies(lineDocument: LineDocument): Boolean = {
      val string = lineDocument.contents.head.toString.trim

      string.nonEmpty && {
        val char = string.head
        char.isLetter && char.isLower
      }
    }

    @tailrec
    def loop(index: Int): Unit = {
      if (index < contents.length) {
        val top = contents(index)
        textRanges += top

        if (index + 2 < contents.length) {
          val mid = contents(index + 1)
          val bot = contents(index + 2)
          // Order these for efficiency.
          val qualifies = midQualifies(mid) && topQualifies(top) && botQualifies(bot)

          if (qualifies) loop(index + 2) // skip middle
          else loop(index + 1)
        }
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
