package org.clulab.pdf2txt.apps

import org.clulab.pdf2txt.Pdf2txt
import org.clulab.pdf2txt.common.utils.Pdf2txtApp
import org.clulab.pdf2txt.pdf.TextConverter

import java.io.File

object Txt2txtDir extends Pdf2txtApp {
  val inputDirName = args.lift(0).getOrElse(".")
  val outputDirName = args.lift(1).getOrElse(inputDirName + "/txt")

  new File(outputDirName).mkdirs()
  Pdf2txt(new TextConverter()).dir(inputDirName, outputDirName, ".txt", ".txt")
}
