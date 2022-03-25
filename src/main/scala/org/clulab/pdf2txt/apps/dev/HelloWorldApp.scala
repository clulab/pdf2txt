package org.clulab.pdf2txt.apps.dev

import org.clulab.pdf2txt.Pdf2txt
import org.clulab.pdf2txt.common.utils.Pdf2txtAppish

object HelloWorldApp extends Pdf2txtAppish {
  val appMessage = args.lift(0).getOrElse(getArgString("apps.HelloWorldApp.message", Some("App message not found!")))
  logger.info(appMessage)

  val pdf2txt = Pdf2txt()
  val classMessage = pdf2txt.getArgString("Pdf2txt.message", Some("Class message not found!"))
  logger.info(classMessage)
}
