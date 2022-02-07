package org.clulab.pdf2txt.preprocessor

import org.clulab.pdf2txt.common.utils.{TextRange, TextRanges}

import java.io.{File, PrintWriter}
import java.util.regex.Pattern

class NumbersPreprocessor(loggerOpt: Option[NumbersLogger] = None) extends Preprocessor {

  def preprocess(textRange: TextRange): TextRanges = {
    val matcher = NumbersPreprocessor.pattern.matcher(textRange.text)

    val buffer = new StringBuffer()
    while (matcher.find()) {
        matcher.appendReplacement(buffer, NumbersPreprocessor.replacement(matcher.group(1)))
    }
    matcher.appendTail(buffer)

    val newText = buffer.toString()
    val left = textRange.text.trim()
    val right = newText.trim()
    // Maybe do the above one at a time for the sake of logging?
    loggerOpt.foreach { logger =>
      logger.log(left, right)
    }
    TextRanges(TextRange(newText))
  }
}
object NumbersPreprocessor {
  val pattern = Pattern.compile("(\\d+\\s+,\\d+)")
  def replacement(groupText: String): String = groupText.filterNot(_.isWhitespace)

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
