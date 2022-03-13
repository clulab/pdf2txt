package org.clulab.pdf2txt.apps

import org.clulab.pdf2txt.common.utils.Pdf2txtApp
import org.clulab.pdf2txt.pdftotext.PdfToTextConverter

object PdfToTextApp extends Pdf2txtApp {
  new DirApp(args, new PdfToTextConverter()).run()
}
