package org.clulab.pdf2txt.apps

import org.clulab.pdf2txt.Pdf2txt
import org.clulab.pdf2txt.common.pdf.TextConverter
import org.clulab.pdf2txt.common.utils.Closer.AutoCloser
import org.clulab.pdf2txt.common.utils.{AppUtils, ConfigError, Pdf2txtApp, Pdf2txtException}
import org.clulab.pdf2txt.languageModel.{AlwaysLanguageModel, GigawordLanguageModel, GloveLanguageModel, NeverLanguageModel}
import org.clulab.pdf2txt.pdfminer.PdfMinerConverter
import org.clulab.pdf2txt.pdftotext.PdfToTextConverter
import org.clulab.pdf2txt.preprocessor.{LigaturePreprocessor, LineBreakPreprocessor, LinePreprocessor, NumberPreprocessor, ParagraphPreprocessor, Preprocessor, UnicodePreprocessor, WordBreakByHyphenPreprocessor, WordBreakBySpacePreprocessor}
import org.clulab.pdf2txt.scienceparse.ScienceParseConverter
import org.clulab.pdf2txt.tika.TikaConverter

import java.io.File

class DirApp(args: Array[String], params: Map[String, String] = Map.empty) {
  val mapAndConfig = AppUtils.mkMapAndConfig(args, params, Pdf2txt.config, "conf", "Pdf2txt")

  if (mapAndConfig.contains("help")) {
    AppUtils.showSyntax("org/clulab/pdf2txt/AddDir.syntax.txt", System.out)
    System.exit(0)
  }

  val pdfConverter = {
    val key = "converter"
    // TODO: Don't create it yet.  Wait until later.
    // TODO: Make lazy in case there are no files to process
    mapAndConfig(key) match {
      case "pdfminer" => new PdfMinerConverter()
      case "pdftotext" => new PdfToTextConverter()
      case "scienceparse" => new ScienceParseConverter()
      case "text" => new TextConverter()
      case "tika" => new TikaConverter()
      case value => throw ConfigError(mapAndConfig, key, value)
    }
  }
  val languageModel = {
    val key = "languageModel"

    mapAndConfig(key) match {
      case "always" => new AlwaysLanguageModel()
      case "gigaWord" => GigawordLanguageModel()
      case "glove" => GloveLanguageModel()
      case "never" => new NeverLanguageModel()
      case value => throw ConfigError(mapAndConfig, key, value)
    }
  }
  val preprocessors = {
    def map(key: String, value: => Preprocessor): Option[Preprocessor] =
        if (mapAndConfig.getBoolean(key)) Some(value) else None

    Array(
      map("line", new LinePreprocessor()),
      map("paragraph", new ParagraphPreprocessor()),
      map("unicode", new UnicodePreprocessor()),
      map("number", new NumberPreprocessor()),
      map("ligature", new LigaturePreprocessor(languageModel)),
      map("lineBreak", new LineBreakPreprocessor(languageModel)),
      map("wordBreakByHyphen", new WordBreakByHyphenPreprocessor()),
      map("wordBreakBySpace", new WordBreakBySpacePreprocessor())
    ).flatten
  }
  val inFileOrDirectory = mapAndConfig("in")
  val outFileOrDirectory = mapAndConfig("out")
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

  // TODO: Print effective configuration

  def runFile(): Unit = {
    val pdf2txt = new Pdf2txt(pdfConverter, preprocessors)

    pdfConverter.autoClose { pdfConverter =>
      pdf2txt.file(inFileOrDirectory, outFileOrDirectory)
    }
  }

  def runDir(): Unit = {
    val pdf2txt = new Pdf2txt(pdfConverter, preprocessors)

    pdfConverter.autoClose { pdfConverter =>
      pdf2txt.dir(inFileOrDirectory, outFileOrDirectory)
    }
  }

  def run(): Unit = {
    if (isFileMode) runFile()
    else runDir()
  }
}

object DirApp extends Pdf2txtApp {
  new DirApp(args).run()
}
