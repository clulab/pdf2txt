package org.clulab.pdf2txt

import org.clulab.pdf2txt.common.utils.Closer.AutoCloser
import org.clulab.pdf2txt.common.utils.{FileUtils, Logging, Pdf2txtConfigured, TextRange}
import org.clulab.pdf2txt.common.utils.StringUtils._
import org.clulab.pdf2txt.preprocessor.{LineBreakPreprocessor, ParagraphPreprocessor, Preprocessor, UnicodePreprocessor, WordBreakPreprocessor}
import org.clulab.pdf2txt.tika.Tika

import java.io.{File, FileInputStream, InputStream, PrintWriter}

class Pdf2txt() extends Pdf2txtConfigured {
  val tika = new Tika()
  val preprocessors = Pdf2txt.preprocessors

  def logError(throwable: Throwable, message: String): String = {
    Pdf2txt.logger.error(message, throwable)
    throw throwable
    message
  }

  def read(inputStream: InputStream): String = {
    try {
      tika.read(inputStream)
    }
    catch {
      case throwable: Throwable => logError(throwable, s"Tika failed to read.")
    }
  }

  def process(rawText: String): String = {
    preprocessors.foldLeft(rawText) { (rawText, preprocessor) =>
      preprocessor.preprocess(TextRange(rawText)).toString
    }
  }

  def write(printWriter: PrintWriter, text: String): Unit = {
    try {
      printWriter.println(text)
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

  def dir(dir: String): Unit = {
    val files = FileUtils.findFiles(dir, ".pdf")

    files.par.foreach { inputFile =>
      try {
        println(s"Converting ${inputFile.getAbsolutePath}...")
        new FileInputStream(inputFile).autoClose { inputStream =>
          val outputFile = new File(inputFile.getAbsolutePath.beforeLast('.', true) + ".txt")

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
  val preprocessors: Array[Preprocessor] = Array(
    new ParagraphPreprocessor(),
    new UnicodePreprocessor(),
    new LineBreakPreprocessor(),
    new WordBreakPreprocessor()
  )

  def apply(): Pdf2txt = new Pdf2txt()
}
