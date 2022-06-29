package org.clulab.pdf2txt.languageModel

class ProbabilisticLanguageModel extends LanguageModel {

  def p(nextWord: String, prevWords: Seq[String]): Float = 0 // TODO

  // We should check if P("dis"|"They decided to") is larger than P("disclose"|"They decided to").
  // If not, then the two tokens should be merged into one.
  override def shouldJoin(left: String, right: String, prevWords: Seq[String]): Boolean = {
    val pUncombined = p(left, prevWords)
    val pCombined = p(left + right, prevWords)

    pCombined != 0f && pCombined >= pUncombined // Favor the combined unless we're already at 0.
  }
}

object ProbabilisticLanguageModel {
  lazy val instance = new ProbabilisticLanguageModel()
}
