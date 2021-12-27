package org.clulab.pdf2txt.common.utils

import org.scalatest.flatspec.{AnyFlatSpec => FlatSpec}
import org.scalatest.matchers.should.{Matchers => Matchers}

class Test extends FlatSpec with Matchers {

  def escape(string: String): String = string.flatMap { char =>
    char match {
      case '\n' => "\\n"
      case '\r' => "\\r"
      case '\t' => "\\t"
      case _ => char.toString
    }
  }
}
