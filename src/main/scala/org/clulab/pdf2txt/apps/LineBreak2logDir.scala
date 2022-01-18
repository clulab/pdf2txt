package org.clulab.pdf2txt.apps

import org.clulab.pdf2txt.common.utils.TextRange
import org.clulab.pdf2txt.languageModel.{AlwaysLanguageModel, NeverLanguageModel}
import org.clulab.pdf2txt.preprocessor.{LineBreakPreprocessor, WordBreakByHyphenPreprocessor}
import org.clulab.utils.FileUtils

import java.io.File

object LineBreak2logDir extends App {

  class Logger {
    var fileOpt: Option[File] = None

    def setFile(file: File): Unit = fileOpt = Option(file)

    def log(message: String): Unit = {
      val name = fileOpt.map(_.getName).getOrElse("")

      println(s"$name\t$message")
    }
  }

  class LoggingLanguageModel(logger: Logger) extends AlwaysLanguageModel {

    override def shouldJoin(left: String, right: String, prevWords: Seq[String]): Boolean = {
      val message = prevWords.mkString(" ") + (if (prevWords.nonEmpty) " " else "") + left + "-" + right

      logger.log(message)
      super.shouldJoin(left, right, prevWords)
    }
  }

  val dir = args.lift(0).getOrElse(".")
  val files = FileUtils.findFiles(dir, ".txt")
  val logger = new Logger()
  val languageModel = new LoggingLanguageModel(logger)
  val preprocessor = new LineBreakPreprocessor(languageModel)

  files.foreach { inputFile =>
    val txt = FileUtils.getTextFromFile(inputFile)

    logger.setFile(inputFile)
    preprocessor.preprocess(TextRange(txt))
  }
}
