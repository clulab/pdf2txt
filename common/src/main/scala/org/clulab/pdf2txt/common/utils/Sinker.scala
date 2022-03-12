package org.clulab.pdf2txt.common.utils

import java.io.BufferedOutputStream
import java.io.{File, FileOutputStream, OutputStreamWriter, PrintWriter}
import java.nio.charset.StandardCharsets

class Sink(file: File, charsetName: String, append: Boolean = false) extends OutputStreamWriter(
  if (append) Sinker.newAppendingBufferedOutputStream(file)
  else Sinker.newBufferedOutputStream(file),
  charsetName
)

object Sinker {
  val utf8: String = StandardCharsets.UTF_8.toString

  def printWriterFromFile(file: File, append: Boolean): PrintWriter =
      new PrintWriter(new Sink(file, utf8, append))

  def newAppendingBufferedOutputStream(file: File): BufferedOutputStream =
      new BufferedOutputStream(new FileOutputStream(file, true))

  def newBufferedOutputStream(file: File): BufferedOutputStream =
      new BufferedOutputStream(new FileOutputStream(file))
}
