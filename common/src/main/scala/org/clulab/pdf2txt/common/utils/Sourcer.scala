package org.clulab.pdf2txt.common.utils

import java.io.FileNotFoundException

import scala.io.BufferedSource
import scala.io.{Codec, Source}

object Sourcer {

  def sourceFromResource(path: String): BufferedSource = {
    val url = Option(Sourcer.getClass.getResource(path))
      .getOrElse(throw new FileNotFoundException(path))

    Source.fromURL(url)(Codec.UTF8)
  }

  def newFileNotFoundException(path: String): FileNotFoundException = {
    val message1 = path + " (The system cannot find the path specified"
    val message2 = message1 + (if (path.startsWith("~")) ".  Make sure to not use the tilde (~) character in paths in lieu of the home directory." else "")
    val message3 = message2 + ")"

    new FileNotFoundException(message3)
  }
}
