package org.clulab.pdf2txt.languageModel

trait LanguageModel {

  /**
   * Say whether the sentence should be
   *   prevWords leftright
   * rather than
   *   prevWords left right
   */
  def shouldJoin(left: String, right: String, prevWords: Seq[String]): Boolean

  /**
   * If withMiddle is true,
   *
   * say whether the sentence should be
   *   prevWords leftmiddleright
   * rather than
   *   prevWords left middle right
   *
   * If withMiddle is False,
   *
   * say whether the sentence should be
   *   prevWords leftright
   * rather than
   *   prevWords left middle right
   */
  def shouldJoin(left: String, middle: String, right: String, prevWords: Seq[String], withMiddle: Boolean = true): Boolean =
      if (withMiddle) shouldJoin(left + middle, right, prevWords)
      else shouldJoin(left, right, prevWords)
}
