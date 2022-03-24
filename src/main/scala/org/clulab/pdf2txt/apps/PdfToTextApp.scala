package org.clulab.pdf2txt.apps

import org.clulab.pdf2txt.common.utils.Pdf2txtAppish

object PdfToTextApp extends Pdf2txtAppish {
  new DirApp(args, Map("converter" -> "pdftotext")).run()
}
