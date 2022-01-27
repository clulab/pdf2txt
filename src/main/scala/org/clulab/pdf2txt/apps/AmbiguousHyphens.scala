package org.clulab.pdf2txt.apps

import org.clulab.pdf2txt.common.utils.StringUtils
import org.clulab.pdf2txt.languageModel.{DictionaryLanguageModel, GloveLanguageModel}

object AmbiguousHyphens extends App {
  val words = GloveLanguageModel().words
  var index = 0

  def isHyphen(char: Char): Boolean = char == StringUtils.HYPHEN

  words.foreach { word =>
    if (word.exists(isHyphen)) {
      val hyphenless = word.filterNot(isHyphen)

      if (words.contains(hyphenless)) {
        index += 1
        println(s"$index\t$word\t$hyphenless")
      }
    }
  }
}
