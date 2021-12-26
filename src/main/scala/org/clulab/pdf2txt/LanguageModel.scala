package org.clulab.pdf2txt

class LanguageModel {

  def p(nextWord: String, prevWords: Seq[String]): Float = ???

  // We should check if P("dis"|"They decided to") is larger than P("disclose"|"They decided to").
  // If not, then the two tokens should be merged into one.
  def shouldJoin(left: String, right: String, prevWords: Seq[String]): Boolean = {
    val pCombined = p(left + right, prevWords)
    val pUncombined = p(left, prevWords)

    pCombined > pUncombined
  }
}

object LanguageModel {
  lazy val instance = new LanguageModel()
}
