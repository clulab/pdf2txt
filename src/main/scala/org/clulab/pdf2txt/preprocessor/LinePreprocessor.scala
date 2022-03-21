package org.clulab.pdf2txt.preprocessor

import org.clulab.pdf2txt.common.utils.{TextRange, TextRanges}
import org.clulab.pdf2txt.document.physical.DocumentByLine

import scala.annotation.tailrec

class LinePreprocessor extends Preprocessor {

  def preprocess(textRange: TextRange): TextRanges = {
    val document = DocumentByLine(textRange)
    val textRanges = new TextRanges()
    val contents = document.contents

    @tailrec
    def loop(index: Int): Unit = {
      if (index < contents.length) {
        val top = contents(index)
        textRanges += top

        if (index + 2 < contents.length) {
          val mid = contents(index + 1)
          val bot = contents(index + 2)

          val qualifies0 = true
          val qualifies1 = qualifies0 && !top.contents.head.toString.trim.endsWith(".") // add not empty
          val qualifies2 = qualifies1 && mid.contents.head.toString.trim.isEmpty
          val qualifies3 = qualifies2 && {
            val charOpt = bot.contents.head.toString.trim.headOption // already has not empty, but do the same

            charOpt.map { char =>
              char.isLetter && char.isLower
            }.getOrElse(false)
          }
          val qualifies = qualifies3

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
