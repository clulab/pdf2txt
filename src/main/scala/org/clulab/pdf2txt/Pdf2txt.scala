package org.clulab.pdf2txt

import org.clulab.pdf2txt.common.utils.Closer.AutoCloser
import org.clulab.pdf2txt.common.utils.{FileUtils, Logging, Pdf2txtConfigured, StringUtils}
import org.clulab.pdf2txt.tika.Tika

import java.io.{File, FileInputStream, InputStream, PrintWriter}

class Pdf2txt() extends Pdf2txtConfigured {
  val tika = new Tika()

  def convert(inputStream: InputStream, printWriter: PrintWriter): Unit = {
    val text = try {
      tika.read(inputStream)
    }
    catch {
      case throwable: Throwable =>
        Pdf2txt.logger.error(s"Tika failed to read.", throwable)
        throw throwable
    }

    try {
      printWriter.println(text)
    }
    catch {
      case throwable: Throwable =>
        Pdf2txt.logger.error(s"PrintWriter failed to write.", throwable)
        throw throwable
    }
  }

  def run(dir: String): Unit = {
    val files = FileUtils.findFiles(dir, ".pdf")

    files.par.foreach { file =>
      try {
        println(s"Converting ${file.getAbsolutePath}...")
        new FileInputStream(file).autoClose { inputStream =>
          val outputFilename = StringUtils.beforeLast(file.getAbsolutePath, '.', true) + ".txt"

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

object Pdf2txt extends App with Logging {
  val dir = args.lift(0).getOrElse(".")

  new Pdf2txt().run(dir)
}
