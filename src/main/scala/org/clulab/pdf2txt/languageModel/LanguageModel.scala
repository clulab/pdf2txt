package org.clulab.pdf2txt.languageModel

trait LanguageModel {

  def shouldJoin(left: String, right: String, prevWords: Seq[String]): Boolean

  def shouldJoin(left: String, middle: String, right: String, prevWords: Seq[String]): Boolean =
      shouldJoin(left + middle, right, prevWords)
}
