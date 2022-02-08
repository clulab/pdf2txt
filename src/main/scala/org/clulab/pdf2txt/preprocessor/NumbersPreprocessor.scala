package org.clulab.pdf2txt.preprocessor

import org.clulab.pdf2txt.common.utils.{TextRange, TextRanges}

import java.io.{File, PrintWriter}
import java.util.regex.Pattern
import scala.annotation.tailrec

class NumbersPreprocessor(hyperparameters: NumbersPreprocessor.Hyperparameters = NumbersPreprocessor.Hyperparameters(), loggerOpt: Option[NumbersLogger] = None) extends Preprocessor {

  def process(text: String, pattern: Pattern, replacement: String): String = {
    val matcher = pattern.matcher(text)
    val buffer = new StringBuffer()

    while (matcher.find()) {
      val before = matcher.group(0).toString
      val after = {
        val buffer = new StringBuffer() // TODO: This is a hack!

        matcher.appendReplacement(buffer, replacement)
        buffer.toString
      }

      loggerOpt.foreach { logger =>
        logger.log(before, after)
      }
      buffer.append(after)
    }
    matcher.appendTail(buffer)
    buffer.toString()
  }

  def preprocessWithComma(text: String): String =
      process(text, NumbersPreprocessor.commaPattern, NumbersPreprocessor.commaReplacement)

  def preprocessWithSpace(text: String): String = {

    @tailrec
    def loop(before: String): String = {
      val after = process(before, NumbersPreprocessor.spacePattern, NumbersPreprocessor.spaceReplacement)

      if (before == after) after
      else loop(after)
    }

    if (!hyperparameters.joinWithSpaces) text
    else loop(text)
  }

  def preprocess(textRange: TextRange): TextRanges = {
    val text1 = textRange.text
    // These are ordered so that spaces are removed before commas.
    val text2 = preprocessWithSpace(text1)
    val text3 = preprocessWithComma(text2)

    TextRanges(TextRange(text3))
  }
}

object NumbersPreprocessor {
  val commaPattern = Pattern.compile("(\\d+)\\s+(,\\d+)")
  val commaReplacement = "$1$2"

  // This could probably be "(^|\\s)(\\d+)(\\s+(\\d+))" to match multiple number groups
  // at the same time, but then a filter might be required to remove the \\s+ parts.
  val spacePattern = Pattern.compile("(^|\\s)(\\d+)\\s+(\\d+)") // no comma here
  val spaceReplacement = "$1$2$3"

  case class Hyperparameters(joinWithSpaces: Boolean = false)
}

class NumbersLogger(printWriter: PrintWriter) {
  protected var fileOpt: Option[File] = None

  printWriter.println("file\tbefore\tafter")

  def setFile(file: File): Unit = fileOpt = Option(file)

  def log(before: String, after: String): Unit = {
    val filename = fileOpt.map(_.getName).getOrElse("")
    printWriter.println(s"$filename\t$before\t$after")
  }
}
