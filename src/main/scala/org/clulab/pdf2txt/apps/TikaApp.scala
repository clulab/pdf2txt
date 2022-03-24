package org.clulab.pdf2txt.apps

import org.clulab.pdf2txt.common.utils.Pdf2txtAppish

object TikaApp extends Pdf2txtAppish {
  new DirApp(args, Map("converter" -> "tika")).run()
}
