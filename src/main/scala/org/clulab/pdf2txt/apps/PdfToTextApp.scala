package org.clulab.pdf2txt.apps

import org.clulab.pdf2txt.common.utils.Pdf2txtAppish

object PdfToTextApp extends Pdf2txtAppish {
  new Pdf2txtApp(args, Map(Pdf2txtArgs.CONVERTER -> "pdftotext")).run()
}
