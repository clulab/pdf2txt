package org.clulab.pdf2txt

import org.clulab.pdf2txt.common.utils.Closer.AutoCloser
import org.clulab.pdf2txt.common.utils.{Logging, Pdf2txtConfigured}
import org.clulab.pdf2txt.languageModel.{GigawordLanguageModel, GloveLanguageModel}
import org.clulab.pdf2txt.pdf.{PdfConverter, TikaConverter}
import org.clulab.pdf2txt.preprocessor.{LigaturePreprocessor, LineBreakPreprocessor, ParagraphPreprocessor, Preprocessor, UnicodePreprocessor, WordBreakByHyphenPreprocessor, WordBreakBySpacePreprocessor}
import org.clulab.utils.FileUtils

import java.io.{BufferedInputStream, File, FileInputStream, InputStream, PrintWriter}

class Pdf2txt(pdfConverter: PdfConverter) extends Pdf2txtConfigured {
  val preprocessors = Pdf2txt.preprocessors

  def logError(throwable: Throwable, message: String): String = {
    Pdf2txt.logger.error(message, throwable)
    throw throwable
    message
  }

  def read(inputStream: InputStream): String = {
    try {
      pdfConverter.convert(inputStream)
    }
    catch {
      case throwable: Throwable => logError(throwable, s"Tika failed to read.")
    }
  }

  def process(rawText: String): String = {
    preprocessors.foldLeft(rawText) { (rawText, preprocessor) =>
      preprocessor.preprocess(rawText).toString
    }
  }

  def write(printWriter: PrintWriter, text: String): Unit = {
    try {
      printWriter.print(text)
    }
    catch {
      case throwable: Throwable => logError(throwable, s"PrintWriter failed to write.")
    }
  }

  def convert(inputStream: InputStream, printWriter: PrintWriter): Unit = {
    val rawText = read(inputStream)
    val cookedText = process(rawText)

    write(printWriter, cookedText)
  }

  def dir(inputDirName: String, outputDirName: String, inputExtension: String = ".pdf", outputExtension: String = ".txt"): Unit = {
    val files = FileUtils.findFiles(inputDirName, inputExtension)

    files.par.foreach { inputFile =>
      try {
        println(s"Converting ${inputFile.getAbsolutePath}...")
        // The InputStream must support mark/reset which isn't enforced by the type system.
        // In other words, a simple FileInputStream will throw an exception at runtime.
        new BufferedInputStream(new FileInputStream(inputFile)).autoClose { inputStream =>
          val outputFile = new File(outputDirName + "/" + inputFile.getName.dropRight(inputExtension.length) + outputExtension)

          try {
            FileUtils.printWriterFromFile(outputFile).autoClose { printWriter =>
              convert(inputStream, printWriter)
            }
          }
          catch {
            case throwable: Throwable => logError(throwable, s"Could not process output file ${inputFile.getAbsolutePath}.")
          }
        }
      }
      catch {
        case throwable: Throwable => logError(throwable, s"Could not process input file ${inputFile.getAbsolutePath}.")
      }
    }
  }
}

object Pdf2txt extends Logging {
  val preprocessors: Array[Preprocessor] = {
//    val languageModel = GloveLanguageModel()
    val languageModel = GigawordLanguageModel()

    Array(
      //    new ParagraphPreprocessor(),
      //    new UnicodePreprocessor(),
      new LigaturePreprocessor(languageModel),
      new LineBreakPreprocessor(languageModel),
      new WordBreakByHyphenPreprocessor(),
      new WordBreakBySpacePreprocessor()
    )
  }

  def apply(pdfConverter: PdfConverter = new TikaConverter()): Pdf2txt = new Pdf2txt(pdfConverter)
}
