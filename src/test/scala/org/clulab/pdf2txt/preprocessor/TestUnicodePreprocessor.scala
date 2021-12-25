package org.clulab.pdf2txt.preprocessor

import org.clulab.pdf2txt.common.utils.{Test, TextRange}

class TestUnicodePreprocessor extends Test {

  behavior of "UnicodePreprocessor"

  val alpha = "\u03B1"
  val omega = "\u03C9"
  val unknown = "\u0385"
  val accent = "\u00e3"
  val inputText = s"The $alpha and the $omega but not the $unknown.  What about $accent?"

  def test(options: UnicodeOptions, expectedOutputText: String): Unit = {
    it should s"convert unicode to ascii with $options" in {
      val preprocessor = new UnicodePreprocessor(options)
      val actualOutputText = preprocessor.preprocess(inputText).toString

      expectedOutputText shouldBe actualOutputText
    }
  }

  test(UnicodeOptions(unknownToSpace = false, knownToSpace = false, keepKnownAccent = false),
      s"The alpha and the omega but not the $unknown.  What about a?")
  test(UnicodeOptions(unknownToSpace = false, knownToSpace = false, keepKnownAccent = true),
      s"The alpha and the omega but not the $unknown.  What about $accent?")

  test(UnicodeOptions(unknownToSpace = false, knownToSpace = true, keepKnownAccent = false),
      s"The   and the   but not the $unknown.  What about  ?")
  test(UnicodeOptions(unknownToSpace = false, knownToSpace = true, keepKnownAccent = true),
      s"The   and the   but not the $unknown.  What about  ?") // known to space trumps

  test(UnicodeOptions(unknownToSpace = true, knownToSpace = false, keepKnownAccent = false),
      s"The alpha and the omega but not the  .  What about a?")
  test(UnicodeOptions(unknownToSpace = true, knownToSpace = false, keepKnownAccent = true),
      s"The alpha and the omega but not the  .  What about $accent?")

  test(UnicodeOptions(unknownToSpace = true, knownToSpace = true, keepKnownAccent = false),
      s"The   and the   but not the  .  What about  ?")
  test(UnicodeOptions(unknownToSpace = true, knownToSpace = true, keepKnownAccent = true),
      s"The   and the   but not the  .  What about  ?")
}
