package org.clulab.pdf2txt

import org.clulab.pdf2txt.common.pdf.PdfConverter
import org.clulab.pdf2txt.common.utils.Closer.AutoCloser
import org.clulab.pdf2txt.common.utils.{Logging, Pdf2txtConfigured}
import org.clulab.pdf2txt.languageModel.{GigawordLanguageModel, GloveLanguageModel}
import org.clulab.pdf2txt.preprocessor.{LigaturePreprocessor, LineBreakPreprocessor, NumbersPreprocessor, ParagraphPreprocessor, Preprocessor, UnicodePreprocessor, WordBreakByHyphenPreprocessor, WordBreakBySpacePreprocessor}
import org.clulab.pdf2txt.tika.TikaConverter
import org.clulab.utils.FileUtils

import java.io.File

class Pdf2txt(pdfConverter: PdfConverter) extends Pdf2txtConfigured {
  val preprocessors = Pdf2txt.preprocessors

  def logError(throwable: Throwable, message: String): String = {
    Pdf2txt.logger.error(message, throwable)
    throw throwable
    message
  }

  def read(inputFile: File): String = {
    try {
      pdfConverter.convert(inputFile)
    }
    catch {
      case throwable: Throwable => logError(throwable, s"Could not read input file.")
    }
  }

  def process(rawText: String): String = {
    try {
      preprocessors.foldLeft(rawText) { (rawText, preprocessor) =>
        preprocessor.preprocess(rawText).toString
      }
    }
    catch {
      case throwable: Throwable => logError(throwable, s"Could not process text.")
    }
  }

  def write(outputFile: File, text: String): Unit = {
    try {
      FileUtils.printWriterFromFile(outputFile).autoClose { printWriter =>
        printWriter.print(text)
      }
    }
    catch {
      case throwable: Throwable => logError(throwable, s"Could not write output file.")
    }
  }

  def convert(inputFile: File, outputFile: File): Unit = {
    try {
      println(s"Converting ${inputFile.getAbsolutePath}...")

      val rawText = read(inputFile)
      val cookedText = process(rawText)
      write(outputFile, cookedText)
    }
    catch {
      case throwable: Throwable => logError(throwable, s"Could not convert ${inputFile.getAbsolutePath} to ${outputFile.getAbsolutePath}.")
    }
  }

  def dir(inputDirName: String, outputDirName: String, inputExtension: String = ".pdf", outputExtension: String = ".txt"): Unit = {
    val files = FileUtils.findFiles(inputDirName, inputExtension)

    files.par.foreach { inputFile =>
      val outputFile = new File(outputDirName + "/" + inputFile.getName.dropRight(inputExtension.length) + outputExtension)

      convert(inputFile, outputFile)
    }
  }
}

object Pdf2txt extends Logging {
  val preprocessors: Array[Preprocessor] = {
    // val languageModel = GloveLanguageModel()
    val languageModel = GigawordLanguageModel()

    Array(
      new ParagraphPreprocessor(),
      new UnicodePreprocessor(),
      new NumbersPreprocessor(),
      new LigaturePreprocessor(languageModel),
      new LineBreakPreprocessor(languageModel),
      new WordBreakByHyphenPreprocessor(),
      new WordBreakBySpacePreprocessor()
    )
  }

  def apply(pdfConverter: PdfConverter = new TikaConverter()): Pdf2txt = new Pdf2txt(pdfConverter)
}
