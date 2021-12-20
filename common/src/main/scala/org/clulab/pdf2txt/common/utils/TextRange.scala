package org.clulab.pdf2txt.common.utils

import org.clulab.pdf2txt.common.utils.StringUtils._

class TextRange(text: String, range: Range) {

  def getRawText: String = text.substring(range)

  def isEmpty: Boolean = range.isEmpty

  def getText: String = addText(new StringBuilder()).toString

  def addText(stringBuilder: StringBuilder): Unit = stringBuilder ++ text

  def start: Int = range.start

  def end: Int = range.end
}
