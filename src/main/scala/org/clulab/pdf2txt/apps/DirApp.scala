package org.clulab.pdf2txt.apps

import org.clulab.pdf2txt.Pdf2txt
import org.clulab.pdf2txt.common.pdf.PdfConverter
import org.clulab.pdf2txt.common.utils.Closer.AutoCloser

import java.io.File

class DirApp(args: Array[String], pdfConverter: PdfConverter, inputExtension: String = ".pdf") {
  val inputDirName: String = args(0)
  val outputDirName: String = args(1)

  def run(): Unit = {
    pdfConverter.autoClose { pdfConverter =>
      new File(outputDirName).mkdirs()
      Pdf2txt(pdfConverter).dir(inputDirName, outputDirName, inputExtension = inputExtension)
    }
  }
}
