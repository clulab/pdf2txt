package org.clulab.pdf2txt.apps

import org.clulab.pdf2txt.common.utils.Pdf2txtApp

object ScienceParseApp extends Pdf2txtApp {
  new DirApp(args, Map("converter" -> "scienceparse")).run()
}
