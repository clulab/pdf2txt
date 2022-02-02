package org.clulab.pdf2txt.apps

import org.clulab.pdf2txt.common.utils.TextRange
import org.clulab.pdf2txt.languageModel.{GloveLanguageModel, LanguageModel}
import org.clulab.pdf2txt.preprocessor.{LigaturePreprocessor, NumbersLogger, NumbersPreprocessor}
import org.clulab.utils.Closer.AutoCloser
import org.clulab.utils.FileUtils

import java.io.{File, PrintWriter}

object Number2logDir extends App {
  val dir = args.lift(0).getOrElse(".")
  val outputFilename = args.lift(1).getOrElse("output.tsv")
  val files = FileUtils.findFiles(dir, ".txt")

  FileUtils.printWriterFromFile(outputFilename).autoClose { printWriter =>
    val logger = new NumbersLogger(printWriter)
    val preprocessor = new NumbersPreprocessor(Some(logger))

    files.foreach { inputFile =>
      val text = FileUtils.getTextFromFile(inputFile)

      logger.setFile(inputFile)

      val newText = preprocessor.preprocess(TextRange(text)).toString
      val newFile = "../corpora/Numbers2logDir/" + inputFile.getName

      FileUtils.printWriterFromFile(newFile).autoClose { printWriter =>
        printWriter.print(newText)
      }
    }
  }
}
