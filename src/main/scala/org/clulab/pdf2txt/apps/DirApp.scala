package org.clulab.pdf2txt.apps

import org.clulab.pdf2txt.Pdf2txt
import org.clulab.pdf2txt.common.pdf.{PdfConverter, TextConverter}
import org.clulab.pdf2txt.common.utils.Closer.AutoCloser
import org.clulab.pdf2txt.common.utils.{AppUtils, ConfigError, Pdf2txtApp, Pdf2txtException}
import org.clulab.pdf2txt.languageModel.{AlwaysLanguageModel, GigawordLanguageModel, GloveLanguageModel, LanguageModel, NeverLanguageModel}
import org.clulab.pdf2txt.pdfminer.PdfMinerConverter
import org.clulab.pdf2txt.pdftotext.PdfToTextConverter
import org.clulab.pdf2txt.preprocessor.{LigaturePreprocessor, LineBreakPreprocessor, LinePreprocessor, NumberPreprocessor, ParagraphPreprocessor, Preprocessor, UnicodePreprocessor, WordBreakByHyphenPreprocessor, WordBreakBySpacePreprocessor}
import org.clulab.pdf2txt.scienceparse.ScienceParseConverter
import org.clulab.pdf2txt.tika.TikaConverter

import java.io.File

class DirApp(args: Array[String], params: Map[String, String] = Map.empty) {
  type PdfConverterConstructor = () => PdfConverter
  type LanguageModelConstructor = () => LanguageModel
  type PreprocessorsConstructor = () => Array[Preprocessor]

  val (pdfConverterConstructor, preprocessors, inFileOrDirectory, outFileOrDirectory, isFileMode) = processArgs()

  def processArgs(): (PdfConverterConstructor, Array[Preprocessor], String, String, Boolean) = {
    try {
      val map = org.clulab.utils.StringUtils.argsToMap(args, verbose = false)
      val mapAndConfig = AppUtils.mkMapAndConfig(map, params, Pdf2txt.config, DirApp.CONF, "Pdf2txt")

      AppUtils.checkArgs(DirApp.argKeys, mapAndConfig)
      if (mapAndConfig.contains(DirApp.HELP)) {
        AppUtils.showSyntax("/org/clulab/pdf2txt/DirApp.syntax.txt", System.out)
        System.exit(0)
      }

      val pdfConverterConstructor = {
        val key = DirApp.CONVERTER
        val value = mapAndConfig(key)

        value match {
          case "pdfminer" => () => new PdfMinerConverter()
          case "pdftotext" => () => new PdfToTextConverter()
          case "scienceparse" => () => new ScienceParseConverter()
          case "text" => () => new TextConverter()
          case "tika" => () => new TikaConverter()
          case _ => throw ConfigError(mapAndConfig, key, value)
        }
      }
      val languageModelConstructor = {
        val key = DirApp.LANGUAGE_MODEL
        val value = mapAndConfig(key)

        value match {
          case "always" => () => new AlwaysLanguageModel()
          case "gigaWord" => () => GigawordLanguageModel()
          case "glove" => () => GloveLanguageModel()
          case "never" => () => new NeverLanguageModel()
          case _ => throw ConfigError(mapAndConfig, key, value)
        }
      }
      val preprocessorsConstructor = {
        def map(key: String, value: => Preprocessor): Option[Preprocessor] =
          if (mapAndConfig.getBoolean(key)) Some(value) else None

        () => {
          val languageModel = languageModelConstructor()

          Array(
            map(DirApp.LINE, new LinePreprocessor()),
            map(DirApp.PARAGRAPH, new ParagraphPreprocessor()),
            map(DirApp.UNICODE, new UnicodePreprocessor()),
            map(DirApp.NUMBER, new NumberPreprocessor()),
            map(DirApp.LIGATURE, new LigaturePreprocessor(languageModel)),
            map(DirApp.LINE_BREAK, new LineBreakPreprocessor(languageModel)),
            map(DirApp.WORD_BREAK_BY_HYPHEN, new WordBreakByHyphenPreprocessor()),
            map(DirApp.WORD_BREAK_BY_SPACE, new WordBreakBySpacePreprocessor())
          ).flatten
        }
      }
      val inFileOrDirectory = mapAndConfig(DirApp.IN)
      val outFileOrDirectory = mapAndConfig(DirApp.OUT)
      val isFileMode = {
        val inFile = new File(inFileOrDirectory)

        if (inFile.isFile) true
        else if (inFile.isDirectory) false
        else if (!inFile.exists) throw new Pdf2txtException(s""""$inFileOrDirectory" can't be found.""")
        else throw new Pdf2txtException(s""""$inFileOrDirectory" can't be identified as a file or directory.""")
      }
      val isModeOk = {
        val outFile = new File(outFileOrDirectory)
        val isModeOk = if (isFileMode) {
          if (outFile.isFile) throw new Pdf2txtException(s"""The file "$inFileOrDirectory" cannot be converted to the existing file "$outFileOrDirectory".""")
          else if (outFile.isDirectory) throw new Pdf2txtException(s"""The file "$inFileOrDirectory" cannot be converted to the existing directory "$outFileOrDirectory".""")
          else if (outFile.exists) throw new Pdf2txtException(s"""The file "$inFileOrDirectory" cannot be converter to the existing "$outFileOrDirectory".""")
          else true
        }
        else {
          if (outFile.isFile) throw new Pdf2txtException(s"""The input directory cannot be converted to the existing file "$outFileOrDirectory".""")
          else if (outFile.isDirectory) true
          else if (outFile.exists) throw new Pdf2txtException(s"""The input directory cannot be converter to the existing "$outFileOrDirectory".""")
          else {
            if (!outFile.mkdirs())
              throw new Pdf2txtException(s"""The output directory "$outFileOrDirectory" could not be created.""")
            true
          }
        }
        assert(isModeOk)
        isModeOk
      }

      AppUtils.showArgs(DirApp.argKeys, mapAndConfig)

      val preprocessors = preprocessorsConstructor()
      (pdfConverterConstructor, preprocessors, inFileOrDirectory, outFileOrDirectory, isFileMode)
    }
    catch {
      case throwable: Throwable =>
        System.err.println(throwable.getMessage)
        System.exit(-1)
        null
    }
  }

  def runFile(): Unit = {
    pdfConverterConstructor().autoClose { pdfConverter =>
      val pdf2txt = new Pdf2txt(pdfConverter, preprocessors)

      pdf2txt.file(inFileOrDirectory, outFileOrDirectory)
    }
  }

  def runDir(): Unit = {
    pdfConverterConstructor().autoClose { pdfConverter =>
      val pdf2txt = new Pdf2txt(pdfConverter, preprocessors)

      pdf2txt.dir(inFileOrDirectory, outFileOrDirectory)
    }
  }

  def run(): Unit = {
    if (isFileMode) runFile()
    else runDir()
  }
}

object DirApp extends Pdf2txtApp {
  val CONF = "conf"
  val HELP = "help"
  val CONVERTER = "converter"
  val LANGUAGE_MODEL = "languageModel"
  val LINE = "line"
  val PARAGRAPH = "paragraph"
  val UNICODE = "unicode"
  val NUMBER = "number"
  val LIGATURE = "ligature"
  val LINE_BREAK = "lineBreak"
  val WORD_BREAK_BY_HYPHEN = "wordBreakByHyphen"
  val WORD_BREAK_BY_SPACE = "wordBreakBySpace"
  val IN = "in"
  val OUT = "out"

  val argKeys: Array[String] = Array(
    CONF,
    HELP,
    CONVERTER,
    LANGUAGE_MODEL,
    LINE,
    PARAGRAPH,
    UNICODE,
    NUMBER,
    LIGATURE,
    LINE_BREAK,
    WORD_BREAK_BY_HYPHEN,
    WORD_BREAK_BY_SPACE,
    IN,
    OUT
  )

  new DirApp(args).run()
}
