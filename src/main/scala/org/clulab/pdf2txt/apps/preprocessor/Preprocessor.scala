package org.clulab.pdf2txt.apps.preprocessor

import org.clulab.pdf2txt.common.utils.TextRange

abstract class Preprocessor extends {

  def preprocess(rawText: String, range: Range): String = {
    val stringBuilder = new StringBuilder()

    preprocess(rawText, range, stringBuilder)
    stringBuilder.toString
  }

  def preprocess(rawText: String, range: Range, stringBuilder: StringBuilder): Unit
}
