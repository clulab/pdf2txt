package org.clulab.pdf2txt.apps.log

import org.clulab.pdf2txt.common.utils.Closer.AutoCloser
import org.clulab.pdf2txt.common.utils.{FileUtils, TextRange}
import org.clulab.pdf2txt.preprocessor.{NumberPreprocessor, NumbersLogger}

import java.io.File

object Number2logDir extends App {
  val dir = args.lift(0).getOrElse(".")
  val outputFilename = args.lift(1).getOrElse("output.tsv")
  val files = FileUtils.findFiles(dir, ".txt")

  FileUtils.printWriterFromFile(new File(outputFilename)).autoClose { printWriter =>
    val logger = new NumbersLogger(printWriter)
    val preprocessor = new NumberPreprocessor(loggerOpt = Some(logger))

    files.foreach { inputFile =>
      val text = FileUtils.getTextFromFile(inputFile)

      logger.setFile(inputFile)

      val newText = preprocessor.preprocess(TextRange(text)).toString
      val newFile = "../corpora/Numbers2logDir/" + inputFile.getName

      FileUtils.printWriterFromFile(new File(newFile)).autoClose { printWriter =>
        printWriter.print(newText)
      }
    }
  }
}
