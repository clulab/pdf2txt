package org.clulab.pdf2txt.apps

import org.clulab.pdf2txt.common.utils.Pdf2txtAppish

object PdfMinerApp extends Pdf2txtAppish {
  new DirApp(args, Map("converter" -> "pdfminer")).run()
}
