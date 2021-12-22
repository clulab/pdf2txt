package org.clulab.pdf2txt.apps.preprocessor

import org.clulab.pdf2txt.common.utils.TextRange

abstract class Preprocessor(rawText: String, range: Range) extends TextRange(rawText, range) {

  def getCookedText: String = addCookedText(new StringBuilder()).toString

  def addCookedText(stringBuilder: StringBuilder): Unit
}

trait PreprocessorConstructor {
  def apply(rawText: String): Preprocessor
}
