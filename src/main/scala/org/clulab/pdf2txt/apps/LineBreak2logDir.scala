package org.clulab.pdf2txt.apps

import org.clulab.pdf2txt.common.utils.TextRange
import org.clulab.pdf2txt.languageModel.AlwaysLanguageModel
import org.clulab.pdf2txt.preprocessor.LineBreakPreprocessor
import org.clulab.utils.Closer.AutoCloser
import org.clulab.utils.FileUtils

import java.io.{File, PrintWriter}

object LineBreak2logDir extends App {

  class Logger(printWriter: PrintWriter) {
    protected var fileOpt: Option[File] = None

    printWriter.println("file\tleft\tright\tcontext")

    def setFile(file: File): Unit = fileOpt = Option(file)

    def log(left: String, right: String, context: String): Unit = {
      val filename = fileOpt.map(_.getName).getOrElse("")

      printWriter.println(s"$filename\t$left\t$right\t$context")
    }
  }

  class LoggingLanguageModel(logger: Logger) extends AlwaysLanguageModel {

    override def shouldJoin(left: String, right: String, prevWords: Seq[String]): Boolean = {
      val context = prevWords.mkString(" ") + (if (prevWords.nonEmpty) " " else "") + left + "-" + right

      logger.log(left, right, context)
      super.shouldJoin(left, right, prevWords)
    }
  }

  val dir = args.lift(0).getOrElse(".")
  val outputFilename = args.lift(1).getOrElse("output.tsv")
  val files = FileUtils.findFiles(dir, ".txt")

  FileUtils.printWriterFromFile(outputFilename).autoClose { printWriter =>
    val logger = new Logger(printWriter)
    val languageModel = new LoggingLanguageModel(logger)
    val preprocessor = new LineBreakPreprocessor(languageModel)

    files.foreach { inputFile =>
      val txt = FileUtils.getTextFromFile(inputFile)

      logger.setFile(inputFile)
      preprocessor.preprocess(TextRange(txt))
    }
  }
}
