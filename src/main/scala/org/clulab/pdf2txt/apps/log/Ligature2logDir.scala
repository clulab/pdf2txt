package org.clulab.pdf2txt.apps.log

import org.clulab.pdf2txt.common.utils.Closer.AutoCloser
import org.clulab.pdf2txt.common.utils.TextRange
import org.clulab.pdf2txt.languageModel.{GloveLanguageModel, LanguageModel}
import org.clulab.pdf2txt.preprocessor.LigaturePreprocessor
import org.clulab.utils.FileUtils

import java.io.{File, PrintWriter}

object Ligature2logDir extends App {

  class Logger(printWriter: PrintWriter) {
    protected var fileOpt: Option[File] = None

    printWriter.println("file\tleft\tright\tjoin\tcontext")

    def setFile(file: File): Unit = fileOpt = Option(file)

    def log(left: String, right: String, join: Boolean, context: String): Unit = {
      val filename = fileOpt.map(_.getName).getOrElse("")
      val joinString = if (join) "T" else "F"

      printWriter.println(s"$filename\t$left\t$right\t$joinString\t$context")
    }
  }

  class LoggingLanguageModel(languageModel: LanguageModel, logger: Logger) extends LanguageModel {

    override def shouldJoin(left: String, right: String, prevWords: Seq[String]): Boolean = {
      val context = prevWords.mkString(" ") + (if (prevWords.nonEmpty) " " else "") + "[" + left + right + "]"
      val result = languageModel.shouldJoin(left, right, prevWords)

      logger.log(left, right, result, context)
      result
    }
  }

  val dir = args.lift(0).getOrElse(".")
  val outputFilename = args.lift(1).getOrElse("output.tsv")
  val files = FileUtils.findFiles(dir, ".txt")

  FileUtils.printWriterFromFile(outputFilename).autoClose { printWriter =>
    val logger = new Logger(printWriter)
    val innerLanguageModel = GloveLanguageModel()
    val outerLanguageModel = new LoggingLanguageModel(innerLanguageModel, logger)
    val preprocessor = new LigaturePreprocessor(outerLanguageModel)

    files.foreach { inputFile =>
      val text = FileUtils.getTextFromFile(inputFile)

      logger.setFile(inputFile)

      val newText = preprocessor.preprocess(TextRange(text)).toString
      val newFile = "../corpora/Ligature2logDir/" + inputFile.getName

      FileUtils.printWriterFromFile(newFile).autoClose { printWriter =>
        printWriter.print(newText)
      }
    }
  }
}
