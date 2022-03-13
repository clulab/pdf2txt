package org.clulab.pdf2txt.apps

import org.clulab.pdf2txt.common.utils.Pdf2txtApp
import org.clulab.pdf2txt.pdfminer.PdfMinerConverter

object PdfMinerApp extends Pdf2txtApp {
  new DirApp(args, new PdfMinerConverter()).run()
}
