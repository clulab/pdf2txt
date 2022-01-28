package org.clulab.pdf2txt.languageModel

class AlwaysLanguageModel() extends LanguageModel {

  def shouldJoin(left: String, right: String, prevWords: Seq[String]): Boolean = true
}
