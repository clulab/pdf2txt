package org.clulab.pdf2txt

import com.typesafe.config.Config
import org.clulab.pdf2txt.common.pdf.PdfConverter
import org.clulab.pdf2txt.common.utils.Closer.AutoCloser
import org.clulab.pdf2txt.common.utils.FileUtils
import org.clulab.pdf2txt.common.utils.Logging
import org.clulab.pdf2txt.common.utils.Pdf2txtConfigured
import org.clulab.pdf2txt.common.utils.Pdf2txtException
import org.clulab.pdf2txt.languageModel.{AlwaysLanguageModel, GigawordLanguageModel, GloveLanguageModel, NeverLanguageModel}
import org.clulab.pdf2txt.preprocessor.{LigaturePreprocessor, LineBreakPreprocessor, NumberPreprocessor, ParagraphPreprocessor, Preprocessor, UnicodePreprocessor, WordBreakByHyphenPreprocessor, WordBreakBySpacePreprocessor}
import org.clulab.pdf2txt.tika.TikaConverter

import java.io.File

class Pdf2txt(pdfConverter: PdfConverter, preprocessors: Array[Preprocessor]) extends Pdf2txtConfigured {

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
      println(s"Converting ${inputFile.getCanonicalPath}...")

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

class ConfigError(config: Config, key: String, value: String, message: String) extends Pdf2txtException(message, null)

object Pdf2txt extends Logging with Pdf2txtConfigured {

  def apply(pdfConverter: PdfConverter = new TikaConverter()): Pdf2txt =
      new Pdf2txt(pdfConverter, getPreprocessors(config))

  def getPreprocessors(config: Config): Array[Preprocessor] = {
    val key = "languageModel"
    val value = config.getString(key)
    val languageModel = value match {
      case "always" => new AlwaysLanguageModel()
      case "gigaWord" => GigawordLanguageModel()
      case "glove" => GloveLanguageModel()
      case "never" => new NeverLanguageModel()
      case _ => throw new ConfigError(config, key, value, s"""The $key "$value" is not recognized.""")
    }

    def map(key: String, value: => Preprocessor): Option[Preprocessor] =
        if (config.getBoolean(key)) Some(value) else None

    Array(
      map("paragraph", new ParagraphPreprocessor()),
      map("unicode", new UnicodePreprocessor()),
      map("number", new NumberPreprocessor()),
      map("ligature", new LigaturePreprocessor(languageModel)),
      map("lineBreak", new LineBreakPreprocessor(languageModel)),
      map("wordBreakByHyphen", new WordBreakByHyphenPreprocessor()),
      map("wordBreakBySpace", new WordBreakBySpacePreprocessor())
    ).flatten
  }
}
