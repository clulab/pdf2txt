package org.clulab.pdf2txt.apps

import org.clulab.pdf2txt.Pdf2txt
import org.clulab.pdf2txt.common.utils.Pdf2txtAppish
import org.clulab.pdf2txt.pdfminer.PdfMinerConverter
import org.clulab.pdf2txt.pdftotext.PdfToTextConverter
import org.clulab.pdf2txt.tika.TikaConverter

import java.io.File

object Pdf2txtDir extends Pdf2txtAppish {
  val inputDirName = args.lift(0).getOrElse(".")
  val outputDirName = args.lift(1).getOrElse(inputDirName + "/txt")
  // This is just a way to keep it from complaining about unused imports.
  val pdfConverter = 2 match {
    case 1 => new TikaConverter()
    case 2 => new PdfToTextConverter()
    case 3 => new PdfMinerConverter()
  }

  new File(outputDirName).mkdirs()
  Pdf2txt(pdfConverter).dir(inputDirName, outputDirName)
}
