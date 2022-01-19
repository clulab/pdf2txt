package org.clulab.pdf2txt.languageModel

trait LanguageModel {
  def shouldJoin(left: String, right: String, prevWords: Seq[String]): Boolean
}
