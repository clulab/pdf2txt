package org.clulab.pdf2txt.common.utils

trait CookedText {

  def getCookedText: String = addCookedText(new StringBuilder()).toString

  def addCookedText(stringBuilder: StringBuilder): Unit
}
