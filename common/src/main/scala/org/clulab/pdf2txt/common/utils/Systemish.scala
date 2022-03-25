package org.clulab.pdf2txt.common.utils

import java.io.PrintStream

trait Systemish {
  val out: PrintStream
  val err: PrintStream

  def exit(status: Int): Unit
}

class StandardSystem() extends Systemish {
  val out: PrintStream = System.out
  val err: PrintStream = System.err

  def exit(status: Int): Unit = System.exit(status)
}

class CustomSystem(val out: PrintStream, val err: PrintStream) extends Systemish {
  var statusOpt: Option[Int] = None

  def exit(status: Int): Unit = statusOpt = Some(status)
}
