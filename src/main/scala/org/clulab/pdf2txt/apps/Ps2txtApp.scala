package org.clulab.pdf2txt.apps

import org.clulab.pdf2txt.common.utils.{FileEditor, FileUtils, TextRange, TextRanges}

import java.io.File
import scala.io.Source
import scala.util.Using

object Ps2txtApp extends App {
  val psFile = new File(args(0))
  val txtFile = new FileEditor(psFile).setExt(".txt").get

  Using.resource(FileUtils.printWriterFromFile(txtFile)) { printWriter =>
    Using.resource(Source.fromFile(psFile)) { source =>
      source.getLines().foreach { line =>
        val text = TextRange(line)
            .findAll("\\(.\\)".r)
            .map(_(1))
            .mkString

        printWriter.println(text)
      }
    }
  }
}
