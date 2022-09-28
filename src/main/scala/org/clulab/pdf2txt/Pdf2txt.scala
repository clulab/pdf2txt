package org.clulab.pdf2txt

import com.typesafe.config.Config
import org.clulab.pdf2txt.common.pdf.PdfConverter
import org.clulab.pdf2txt.common.utils.Closer.AutoCloser
import org.clulab.pdf2txt.common.utils.{ConfigError, FileUtils, Logging, MetadataHolder, Pdf2txtConfigured, Pdf2txtException, Preprocessor}
import org.clulab.pdf2txt.languageModel.{AlwaysLanguageModel, GigawordLanguageModel, GloveLanguageModel, NeverLanguageModel}
import org.clulab.pdf2txt.preprocessor.{CasePreprocessor, LigaturePreprocessor, LineBreakPreprocessor, LinePreprocessor, NumberPreprocessor, ParagraphPreprocessor, UnicodePreprocessor, WordBreakByHyphenPreprocessor, WordBreakBySpacePreprocessor}
import org.clulab.pdf2txt.tika.TikaConverter
import org.clulab.utils.ThreadUtils

import java.io.File
import scala.annotation.tailrec

class Pdf2txt(pdfConverter: PdfConverter, preprocessors: Array[Preprocessor]) extends Pdf2txtConfigured {

  def logError(throwable: Throwable, message: String): String = {
    Pdf2txt.logger.error(message, throwable)
    throw throwable
    message
  }

  def read(inputFile: File, metadataHolderOpt: Option[MetadataHolder]): String = {
    try {
      pdfConverter.convert(inputFile, metadataHolderOpt)
    }
    catch {
      case throwable: Throwable => logError(throwable, s"Could not read input file.")
    }
  }

  def process(rawText: String, maxLoops: Int): String = {
    try {

      @tailrec
      def loop(rawText: String, loopCount: Int, results: Set[String]): String = {
        if (maxLoops != 0 && loopCount >= maxLoops)
          rawText
        else {
          val cookedText = preprocessors.foldLeft(rawText) { (rawText, preprocessor) =>
            preprocessor.preprocess(rawText).toString
          }

          if (results(cookedText)) cookedText
          else loop(cookedText, loopCount + 1, results + cookedText)
        }
      }

      loop(rawText, 0, Set(rawText))
    }
    catch {
      case throwable: Throwable => logError(throwable, s"Could not process text.")
    }
  }

  def writeOut(outputFile: File, text: String): Unit = {
    try {
      FileUtils.printWriterFromFile(outputFile).autoClose { printWriter =>
        printWriter.print(text)
      }
    }
    catch {
      case throwable: Throwable => logError(throwable, s"Could not write output file.")
    }
  }

  def writeMeta(metaFileOpt: Option[File], metadataHolderOpt: Option[MetadataHolder]): Unit = {
    metadataHolderOpt.foreach { metadataHolder =>
      metadataHolder.get.foreach { text =>
        try {
          FileUtils.printWriterFromFile(metaFileOpt.get).autoClose { printWriter =>
            printWriter.print(text)
          }
        }
        catch {
          case throwable: Throwable => logError(throwable, s"Could not write meta file.")
        }
      }
    }
  }

  def convert(inputFile: File, outputFile: File, metaFileOpt: Option[File], loops: Int): Unit = {
    try {
      Pdf2txt.logger.info(s"Converting ${inputFile.getCanonicalPath}...")

      val metadataHolderOpt = metaFileOpt.map { _ => new MetadataHolder(None) }
      val rawText = read(inputFile, metadataHolderOpt)
      val cookedText = process(rawText, loops)
      writeOut(outputFile, cookedText)
      writeMeta(metaFileOpt, metadataHolderOpt)
    }
    catch {
      case throwable: Throwable => logError(throwable, s"Could not convert ${inputFile.getAbsolutePath} to ${outputFile.getAbsolutePath}.")
    }
  }

  def file(inputFileName: String, outputFileName: String, metaFileNameOpt: Option[String] = None, loops: Int = 1, overwrite: Boolean = false): Unit = {
    val inputFile = new File(inputFileName)
    val outputFile = new File(outputFileName)
    val metaFileOpt = metaFileNameOpt.map(new File(_))

    val skipOutput = outputFile.exists && !overwrite
    if (skipOutput)
      Pdf2txt.logger.warn(s"""For input file "${inputFile.getPath}" the output file "${outputFile.getPath}" already exists.""")
    val skipMeta = metaFileOpt.isDefined && metaFileOpt.get.exists && !overwrite
    if (skipMeta)
      Pdf2txt.logger.warn(s"""For input file "${inputFile.getPath}" the meta file "${metaFileOpt.get.getPath}" already exists.""")
    if (!skipOutput && !skipMeta)
      convert(inputFile, outputFile, metaFileOpt, loops)
  }

  def dir(inputDirName: String, outputDirName: String, metaDirNameOpt: Option[String] = None, threads: Int = 0, loops: Int = 1, overwrite: Boolean = false): Unit = {
    val  inputExtension = pdfConverter.inputExtension
    val outputExtension = pdfConverter.outputExtension
    val   metaExtension = pdfConverter.metaExtension
    val files = FileUtils.findFiles(inputDirName, inputExtension)
    val parFiles = threads match {
      case threads if threads <= 0 =>
        val processors = Runtime.getRuntime().availableProcessors()
        ThreadUtils.parallelize(files, processors)
      case 1 => files
      case threads if threads > 1 => ThreadUtils.parallelize(files, threads)
    }

    parFiles.foreach { inputFile =>
      val baseName = inputFile.getName.dropRight(inputExtension.length)
      val outputFile = new File(outputDirName + "/" + baseName + outputExtension)
      val metaFileOpt = metaDirNameOpt.map { metaDirName => new File(metaDirName + "/" + baseName + metaExtension) }

      val skipOutput = outputFile.exists && !overwrite
      if (skipOutput)
        Pdf2txt.logger.warn(s"""For input file "${inputFile.getPath}" the output file "${outputFile.getPath}" already exists.""")
      val skipMeta = metaFileOpt.isDefined && metaFileOpt.get.exists && !overwrite
      if (skipMeta)
        Pdf2txt.logger.warn(s"""For input file "${inputFile.getPath}" the meta file "${metaFileOpt.get.getPath}" already exists.""")
      if (!skipOutput && !skipMeta)
        convert(inputFile, outputFile, metaFileOpt, loops)
    }
  }
}

object Pdf2txt extends Logging with Pdf2txtConfigured {

  def apply(pdfConverter: PdfConverter = new TikaConverter()): Pdf2txt =
      new Pdf2txt(pdfConverter, getPreprocessors(config))

  def getPreprocessors(config: Config): Array[Preprocessor] = {
    val languageModel = {
      val key = "languageModel"
      val value = config.getString(key)

      value match {
        case "always" => new AlwaysLanguageModel()
        case "gigaWord" => GigawordLanguageModel()
        case "glove" => GloveLanguageModel()
        case "never" => new NeverLanguageModel()
        case _ => throw ConfigError(key, value)
      }
    }

    val caseCutoff = {
      val key = "caseCutoff"
      val value = config.getDouble(key).toFloat

      value
    }

    def map(key: String, value: => Preprocessor): Option[Preprocessor] =
        if (config.getBoolean(key)) Some(value) else None

    Array(
      map("line", new LinePreprocessor()),
      map("paragraph", new ParagraphPreprocessor()),
      map("unicode", new UnicodePreprocessor()),
      map("case", new CasePreprocessor(caseCutoff)),
      map("number", new NumberPreprocessor()),
      map("ligature", new LigaturePreprocessor(languageModel)),
      map("lineBreak", new LineBreakPreprocessor(languageModel)),
      map("wordBreakByHyphen", new WordBreakByHyphenPreprocessor(languageModel)),
      map("wordBreakBySpace", new WordBreakBySpacePreprocessor()) // languageModel
    ).flatten
  }
}
