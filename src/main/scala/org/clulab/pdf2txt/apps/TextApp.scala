package org.clulab.pdf2txt.apps

import org.clulab.pdf2txt.common.pdf.TextConverter
import org.clulab.pdf2txt.common.utils.Pdf2txtApp

object TextApp extends Pdf2txtApp {
  new DirApp(args, new TextConverter(), inputExtension = ".txt").run()
}
