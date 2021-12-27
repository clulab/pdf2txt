package org.clulab.pdf2txt.apps

import org.clulab.pdf2txt.Pdf2txt
import org.clulab.pdf2txt.common.utils.Pdf2txtApp

object Pdf2txtDir extends Pdf2txtApp {
  val dir = args.lift(0).getOrElse(".")

  new Pdf2txt().dir(dir)
}
