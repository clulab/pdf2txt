package org.clulab.pdf2txt.apps

import org.clulab.pdf2txt.common.utils.Closer.AutoCloser
import org.clulab.pdf2txt.common.utils.{FileEditor, FileUtils, TextRange, TextRanges}

import java.io.File
import scala.io.Source

object Ps2txtApp extends App {
  val psFile = new File(args(0))
  val txtFile = new FileEditor(psFile).setExt(".txt").get

  FileUtils.printWriterFromFile(txtFile).autoClose { printWriter =>
    Source.fromFile(psFile).autoClose { source =>
      source.getLines.foreach { line =>
        val text = TextRange(line)
            .findAll("\\(.\\)".r)
            .map(_(1))
            .mkString

        printWriter.println(text)
      }
    }
  }
}
