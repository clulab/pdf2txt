package org.clulab.pdf2txt.apps.log

import org.clulab.pdf2txt.common.utils.{FileUtils, TextRange}
import org.clulab.pdf2txt.preprocessor.{NumberPreprocessor, NumbersLogger}

import java.io.File
import scala.util.Using

object Number2logDir extends App {
  val dir = args.lift(0).getOrElse(".")
  val outputFilename = args.lift(1).getOrElse("output.tsv")
  val files = FileUtils.findFiles(dir, ".txt")

  Using.resource(FileUtils.printWriterFromFile(new File(outputFilename))) { printWriter =>
    val logger = new NumbersLogger(printWriter)
    val preprocessor = new NumberPreprocessor(loggerOpt = Some(logger))

    files.foreach { inputFile =>
      val text = FileUtils.getTextFromFile(inputFile)

      logger.setFile(inputFile)

      val newText = preprocessor.preprocess(TextRange(text)).toString
      val newFile = "../corpora/Numbers2logDir/" + inputFile.getName

      Using.resource(FileUtils.printWriterFromFile(new File(newFile))) { printWriter =>
        printWriter.print(newText)
      }
    }
  }
}
