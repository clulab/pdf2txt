package org.clulab.pdf2txt.apps.dir

import org.clulab.pdf2txt.Pdf2txt
import org.clulab.pdf2txt.common.pdf.TextConverter
import org.clulab.pdf2txt.common.utils.Pdf2txtAppish

import java.io.File

object Txt2txtDir extends Pdf2txtAppish {
  val inputDirName = args.lift(0).getOrElse(".")
  val outputDirName = args.lift(1).getOrElse(inputDirName + "/txt")

  new File(outputDirName).mkdirs()
  Pdf2txt(new TextConverter()).dir(inputDirName, outputDirName)
}
