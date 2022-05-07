package org.clulab.pdf2txt.utils

object WordTypes extends Enumeration {
  type WordType = Value
  val AllLower, AllUpper, InitialUpper, NonWord, Other = Value

  def apply(word: String): WordType = {
    val letterCount = word.count(_.isLetter)
    // isLower and isUpper on non-letters will both be false.
    val lowerCount = word.count(_.isLower)
    val upperCount = word.count(_.isUpper)

    if (letterCount == 0) // This also accounts for word.isEmpty.
      NonWord
    else if (lowerCount == letterCount)
      AllLower
    else if (upperCount == letterCount)
      AllUpper
    else if (upperCount == 1 && word.head.isUpper)
      InitialUpper
    else Other
  }
}
