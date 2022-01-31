package org.clulab.pdf2txt.languageModel

class NeverLanguageModel() extends LanguageModel {

  override def shouldJoin(left: String, right: String, prevWords: Seq[String]): Boolean = false
}
