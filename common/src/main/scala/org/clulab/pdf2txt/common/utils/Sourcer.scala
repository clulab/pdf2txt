package org.clulab.pdf2txt.common.utils

import java.io.FileNotFoundException
import java.nio.charset.StandardCharsets

import scala.io.BufferedSource
import scala.io.Source

object Sourcer {
  val utf8: String = StandardCharsets.UTF_8.toString

  def sourceFromResource(path: String): BufferedSource = {
    val url = Option(Sourcer.getClass.getResource(path))
      .getOrElse(throw new FileNotFoundException(path))

    Source.fromURL(url, utf8)
  }
}
