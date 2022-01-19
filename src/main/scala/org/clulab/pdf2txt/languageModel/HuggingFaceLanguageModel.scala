package org.clulab.pdf2txt.languageModel

class HuggingFaceLanguageModel extends ProbabilisticLanguageModel {

  override def p(nextWord: String, prevWords: Seq[String]): Float = ???
}
