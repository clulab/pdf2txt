package org.clulab.pdf2txt.apps.log

import org.clulab.pdf2txt.common.utils.{FileUtils, TextRange}
import org.clulab.pdf2txt.languageModel.NeverLanguageModel
import org.clulab.pdf2txt.preprocessor.WordBreakByHyphenPreprocessor

import java.io.File

object WordBreakByHyphen2logDir extends App {

  class Logger {
    var fileOpt: Option[File] = None

    def setFile(file: File): Unit = fileOpt = Option(file)

    def log(message: String): Unit = {
      val name = fileOpt.map(_.getName).getOrElse("")

      println(s"$name\t$message")
    }
  }

  class LoggingLanguageModel(logger: Logger) extends NeverLanguageModel {

    override def shouldJoin(left: String, right: String, prevWords: Seq[String]): Boolean = {
      val message = prevWords.mkString(" ") + (if (prevWords.nonEmpty) " " else "") + "[" + left + right + "]"

      logger.log(message)
      super.shouldJoin(left, right, prevWords)
    }
  }

  val dir = args.lift(0).getOrElse(".")
  val files = FileUtils.findFiles(dir, ".txt")
  val logger = new Logger()
  val languageModel = new LoggingLanguageModel(logger)
  val preprocessor = new WordBreakByHyphenPreprocessor(languageModel)

  files.foreach { inputFile =>
    val txt = FileUtils.getTextFromFile(inputFile)

    logger.setFile(inputFile)
    preprocessor.preprocess(TextRange(txt))
  }
}
