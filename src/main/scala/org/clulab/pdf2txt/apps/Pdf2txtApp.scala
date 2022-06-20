package org.clulab.pdf2txt.apps

import com.typesafe.config.ConfigBeanFactory
import org.clulab.pdf2txt.BuildInfo
import org.clulab.pdf2txt.Pdf2txt
import org.clulab.pdf2txt.common.pdf.{PdfConverter, TextConverter}
import org.clulab.pdf2txt.common.utils.Closer.AutoCloser
import org.clulab.pdf2txt.common.utils.{AppUtils, ConfigError, Pdf2txtAppish, Pdf2txtException, Preprocessor, StandardSystem, Systemish}
import org.clulab.pdf2txt.languageModel.{AlwaysLanguageModel, GigawordLanguageModel, GloveLanguageModel, LanguageModel, NeverLanguageModel}
import org.clulab.pdf2txt.pdfminer.PdfMinerConverter
import org.clulab.pdf2txt.pdftotext.PdfToTextConverter
import org.clulab.pdf2txt.preprocessor.{CasePreprocessor, LigaturePreprocessor, LineBreakPreprocessor, LinePreprocessor, NumberPreprocessor, ParagraphPreprocessor, UnicodePreprocessor, WordBreakByHyphenPreprocessor, WordBreakBySpacePreprocessor}
import org.clulab.pdf2txt.scienceparse.{AdobeConverter, ScienceParseConverter}
import org.clulab.pdf2txt.tika.TikaConverter

import java.io.File

class Pdf2txtApp(args: Array[String], params: Map[String, String] = Map.empty, system: Systemish = new StandardSystem()) {
  type PdfConverterConstructor = () => PdfConverter
  type LanguageModelConstructor = () => LanguageModel
  type PreprocessorConstructor = () => Preprocessor
  type PreprocessorsConstructor = () => Array[Preprocessor]

  val (pdfConverterConstructor, preprocessors, inFileOrDirectory, outFileOrDirectory, isFileMode, threads, loops, overwrite) = processArgs()

  def processArgs(): (PdfConverterConstructor, Array[Preprocessor], String, String, Boolean, Int, Int, Boolean) = {
    try {
      val map = AppUtils.argsToMap(args)
      val mapAndConfig = AppUtils.mkMapAndConfig(map, params, Pdf2txt.config, Pdf2txtArgs.CONF, "Pdf2txt")

      AppUtils.checkArgs(Pdf2txtArgs.argKeys, mapAndConfig, system)
      if (Pdf2txtArgs.helps.exists(mapAndConfig.contains)) {
        AppUtils.showSyntax("/org/clulab/pdf2txt/Pdf2txtApp.syntax.txt", system.out, BuildInfo.version)
        system.exit(0)
      }

      val adobeCredentialsOpt = mapAndConfig.get(Pdf2txtArgs.ADOBE_CREDENTIALS)
      val numberParameters = ConfigBeanFactory.create(mapAndConfig.config.getConfig(Pdf2txtArgs.NUMBER_PARAMETERS), classOf[NumberPreprocessor.Parameters])

      val pdfConverterConstructor = {
        val key = Pdf2txtArgs.CONVERTER
        val value = mapAndConfig(key)

        value match {
          case Pdf2txtArgs.ADOBE => () => new AdobeConverter(adobeCredentialsOpt)
          case Pdf2txtArgs.PDF_MINER => () => new PdfMinerConverter()
          case Pdf2txtArgs.PDF_TO_TEXT => () => new PdfToTextConverter()
          case Pdf2txtArgs.SCIENCE_PARSE => () => new ScienceParseConverter()
          case Pdf2txtArgs.TEXT => () => new TextConverter()
          case Pdf2txtArgs.TIKA => () => new TikaConverter()
          case _ => throw ConfigError(mapAndConfig, key, value)
        }
      }
      val languageModelConstructor = {
        val key = Pdf2txtArgs.LANGUAGE_MODEL
        val value = mapAndConfig(key)

        value match {
          case Pdf2txtArgs.ALWAYS => () => new AlwaysLanguageModel()
          case Pdf2txtArgs.GIGA_WORD => () => GigawordLanguageModel()
          case Pdf2txtArgs.GLOVE => () => GloveLanguageModel()
          case Pdf2txtArgs.NEVER => () => new NeverLanguageModel()
          case _ => throw ConfigError(mapAndConfig, key, value)
        }
      }
      val caseCutoff = mapAndConfig.getFloat(Pdf2txtArgs.CASE_CUTOFF, Some(CasePreprocessor.defaultCutoff))
      val preprocessorsConstructor = {
        def map(key: String, preprocessorConstructor: PreprocessorConstructor): Option[PreprocessorConstructor] =
          if (mapAndConfig.getBoolean(key)) Some(preprocessorConstructor) else None

        lazy val languageModel = languageModelConstructor()
        val preprocessorConstructors = Array(
          map(Pdf2txtArgs.LINE, () => new LinePreprocessor()),
          map(Pdf2txtArgs.PARAGRAPH, () => new ParagraphPreprocessor()),
          map(Pdf2txtArgs.UNICODE, () => new UnicodePreprocessor()),
          map(Pdf2txtArgs.CASE, () => new CasePreprocessor(caseCutoff)),
          map(Pdf2txtArgs.NUMBER, () => new NumberPreprocessor(numberParameters)),
          map(Pdf2txtArgs.LIGATURE, () => new LigaturePreprocessor(languageModel)),
          map(Pdf2txtArgs.LINE_BREAK, () => new LineBreakPreprocessor(languageModel)),
          map(Pdf2txtArgs.WORD_BREAK_BY_HYPHEN, () => new WordBreakByHyphenPreprocessor()),
          map(Pdf2txtArgs.WORD_BREAK_BY_SPACE, () => new WordBreakBySpacePreprocessor())
        ).flatten

        () => preprocessorConstructors.map(_())
      }
      val inFileOrDirectory = mapAndConfig(Pdf2txtArgs.IN)
      val outFileOrDirectory = mapAndConfig(Pdf2txtArgs.OUT)
      val threads = mapAndConfig.getInt(Pdf2txtArgs.THREADS)
      val loops = mapAndConfig.getInt(Pdf2txtArgs.LOOPS)
      val overwrite = mapAndConfig.getBoolean(Pdf2txtArgs.OVERWRITE)
      val isFileMode = {
        val inFile = new File(inFileOrDirectory)

        if (inFile.isFile) true
        else if (inFile.isDirectory) false
        else if (!inFile.exists) throw new Pdf2txtException(s""""$inFileOrDirectory" can't be found.""")
        else throw new Pdf2txtException(s""""$inFileOrDirectory" can't be identified as a file or directory.""")
      }
      val _ = {
        val outFile = new File(outFileOrDirectory)
        val isModeOk = if (isFileMode) {
          if (outFile.isFile) {
            if (!overwrite)
              throw new Pdf2txtException(s"""The input file "$inFileOrDirectory" cannot be converted to the existing output file "$outFileOrDirectory".""")
            true
          }
          else if (outFile.isDirectory) throw new Pdf2txtException(s"""The input file "$inFileOrDirectory" cannot be converted to the existing output directory "$outFileOrDirectory".""")
          else if (outFile.exists) throw new Pdf2txtException(s"""The input file "$inFileOrDirectory" cannot be converted to the existing output "$outFileOrDirectory".""")
          else true
        }
        else {
          if (outFile.isFile) throw new Pdf2txtException(s"""The input directory cannot be converted to the existing output file "$outFileOrDirectory".""")
          else if (outFile.isDirectory) true
          else if (outFile.exists) throw new Pdf2txtException(s"""The input directory cannot be converter to the existing output "$outFileOrDirectory".""")
          else {
            if (!outFile.mkdirs())
              throw new Pdf2txtException(s"""The output directory "$outFileOrDirectory" could not be created.""")
            true
          }
        }
        assert(isModeOk)
        isModeOk
      }

      AppUtils.showArgs(Pdf2txtArgs.argKeys, mapAndConfig, system.out)

      val preprocessors = preprocessorsConstructor()
      val argsString = AppUtils.mkArgsString(this, Pdf2txtArgs.argKeys, mapAndConfig)

      Pdf2txtApp.logger.info(s"Running $argsString...")
      (pdfConverterConstructor, preprocessors, inFileOrDirectory, outFileOrDirectory, isFileMode, threads, loops, overwrite)
    }
    catch {
      case throwable: Throwable =>
        Option(throwable.getMessage).map { message =>
          system.err.println(message)
        }
        system.exit(-1)
        null
    }
  }

  def runFile(): Unit = {
    pdfConverterConstructor().autoClose { pdfConverter =>
      val pdf2txt = new Pdf2txt(pdfConverter, preprocessors)

      pdf2txt.file(inFileOrDirectory, outFileOrDirectory, loops, overwrite)
    }
  }

  def runDir(): Unit = {
    pdfConverterConstructor().autoClose { pdfConverter =>
      val pdf2txt = new Pdf2txt(pdfConverter, preprocessors)

      pdf2txt.dir(inFileOrDirectory, outFileOrDirectory, threads, loops, overwrite)
    }
  }

  def run(): Unit = {
    if (isFileMode) runFile()
    else runDir()
  }
}

object Pdf2txtArgs {
  val CONF = "conf"
  val HELP1 = "help"
  val HELP2 = "h"
  val CONVERTER = "converter"
  val LANGUAGE_MODEL = "languageModel"
  val LINE = "line"
  val PARAGRAPH = "paragraph"
  val UNICODE = "unicode"
  val CASE = "case"
  val CASE_CUTOFF = "caseCutoff"
  val NUMBER = "number"
  val LIGATURE = "ligature"
  val LINE_BREAK = "lineBreak"
  val WORD_BREAK_BY_HYPHEN = "wordBreakByHyphen"
  val WORD_BREAK_BY_SPACE = "wordBreakBySpace"
  val IN = "in"
  val OUT = "out"
  val THREADS = "threads"
  val LOOPS = "loops"
  val OVERWRITE = "overwrite"
  val ADOBE_CREDENTIALS = "adobeCredentials"
  val NUMBER_PARAMETERS = "numberParameters"

  val helps: Array[String] = Array(HELP1, HELP2)
  val argKeys: Array[String] = Array(
    CONF,
    HELP1,
    HELP2,
    CONVERTER,
    LANGUAGE_MODEL,
    LINE,
    PARAGRAPH,
    UNICODE,
    CASE,
    NUMBER,
    LIGATURE,
    LINE_BREAK,
    WORD_BREAK_BY_HYPHEN,
    WORD_BREAK_BY_SPACE,
    IN,
    OUT,
    THREADS,
    LOOPS,
    OVERWRITE
  )

  val ADOBE = "adobe"
  val PDF_MINER = "pdfminer"
  val PDF_TO_TEXT = "pdftotext"
  val SCIENCE_PARSE = "scienceparse"
  val TEXT = "text"
  val TIKA = "tika"

  val converters: Array[String] = Array(
    ADOBE,
    PDF_MINER,
    PDF_TO_TEXT,
    SCIENCE_PARSE,
    TEXT,
    TIKA
  )

  val ALWAYS = "always"
  val GIGA_WORD = "gigaword"
  val GLOVE = "glove"
  val NEVER = "never"

  val languageModels: Array[String] = Array(
    ALWAYS,
    GIGA_WORD,
    GLOVE,
    NEVER
  )
}

object Pdf2txtApp extends Pdf2txtAppish {
  new Pdf2txtApp(args).run()
}
