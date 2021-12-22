package org.clulab.pdf2txt

import org.clulab.pdf2txt.common.utils.Closer.AutoCloser
import org.clulab.pdf2txt.common.utils.{FileUtils, Logging, Pdf2txtConfigured}
import org.clulab.pdf2txt.common.utils.StringUtils._
import org.clulab.pdf2txt.preprocessor.{LineBreakPreprocessor, ParagraphPreprocessor, Preprocessor, UnicodePreprocessor, WordBreakPreprocessor}
import org.clulab.pdf2txt.tika.Tika

import java.io.{File, FileInputStream, InputStream, PrintWriter}

class Pdf2txt() extends Pdf2txtConfigured {
  val tika = new Tika()
  val preprocessors = Pdf2txt.preprocessors

  def convert(inputStream: InputStream, printWriter: PrintWriter): Unit = {
    val rawText = try {
      tika.read(inputStream)
    }
    catch {
      case throwable: Throwable =>
        Pdf2txt.logger.error(s"Tika failed to read.", throwable)
        throw throwable
    }

    val cookedText = preprocessors.foldLeft(rawText) { (rawText, preprocessor) =>
      preprocessor.preprocess(rawText, rawText.range)
    }

    try {
      printWriter.println(cookedText)
    }
    catch {
      case throwable: Throwable =>
        Pdf2txt.logger.error(s"PrintWriter failed to write.", throwable)
        throw throwable
    }
  }

  def dir(dir: String): Unit = {
    val files = FileUtils.findFiles(dir, ".pdf")

    files.par.foreach { file =>
      try {
        println(s"Converting ${file.getAbsolutePath}...")
        new FileInputStream(file).autoClose { inputStream =>
          val outputFilename = file.getAbsolutePath.beforeLast('.', true) + ".txt"

          try {
            FileUtils.printWriterFromFile(new File(outputFilename)).autoClose { printWriter =>
              convert(inputStream, printWriter)
            }
          }
          catch {
            case throwable: Throwable =>
              Pdf2txt.logger.error(s"Could not process output file $outputFilename.", throwable)
              throw throwable
          }
        }
      }
      catch {
        case throwable: Throwable =>
          Pdf2txt.logger.error(s"Could not process input file ${file.getAbsolutePath}.", throwable)
          throw throwable
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
