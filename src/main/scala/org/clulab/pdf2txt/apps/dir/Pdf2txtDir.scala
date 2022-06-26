package org.clulab.pdf2txt.apps.dir

import org.clulab.pdf2txt.Pdf2txt
import org.clulab.pdf2txt.adobe.AdobeConverter
import org.clulab.pdf2txt.common.pdf.TextConverter
import org.clulab.pdf2txt.common.utils.Pdf2txtAppish
import org.clulab.pdf2txt.pdfminer.PdfMinerConverter
import org.clulab.pdf2txt.pdftotext.PdfToTextConverter
import org.clulab.pdf2txt.scienceparse.ScienceParseConverter
import org.clulab.pdf2txt.tika.TikaConverter

import java.io.File

object Pdf2txtDir extends Pdf2txtAppish {
  val inputDirName = args.lift(0).getOrElse(".")
  val outputDirName = args.lift(1).getOrElse(inputDirName + "/txt")
  // This is just a way to keep it from complaining about unused imports
  // while controlling the converter in code.
  val pdfConverter = 4 match {
    case 1 => new AdobeConverter()
    case 2 => new PdfMinerConverter()
    case 3 => new ScienceParseConverter()
    case 4 => new TikaConverter()
    case 5 => new TextConverter()
    case 6 => new PdfToTextConverter()
  }

  new File(outputDirName).mkdirs()
  Pdf2txt(pdfConverter).dir(inputDirName, outputDirName)
}
