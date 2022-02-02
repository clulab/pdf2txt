package org.clulab.pdf2txt.preprocessor

import org.clulab.pdf2txt.common.utils.{TextRange, TextRanges}

import java.io.{File, PrintWriter}
import java.util.regex.Pattern

class NumbersPreprocessor(loggerOpt: Option[NumbersLogger] = None) extends Preprocessor {

  def preprocess(textRange: TextRange): TextRanges = {
    val matcher = NumbersPreprocessor.pattern.matcher(textRange.text)
    val newText = matcher.replaceAll(NumbersPreprocessor.replacement)

    // Maybe do the above one at a time for the sake of logging?
    loggerOpt.foreach { logger =>
      logger.log("left", "right")
    }
    TextRanges(TextRange(newText))
  }
}

object NumbersPreprocessor {
  val pattern = Pattern.compile("(hello)")
  val replacement = "$1 there"
}

class NumbersLogger(printWriter: PrintWriter) {
  protected var fileOpt: Option[File] = None

  printWriter.println("file\tleft\tright")

  def setFile(file: File): Unit = fileOpt = Option(file)

  def log(left: String, right: String): Unit = {
    val filename = fileOpt.map(_.getName).getOrElse("")

    printWriter.println(s"$filename\t$left\t$right")
  }
}
