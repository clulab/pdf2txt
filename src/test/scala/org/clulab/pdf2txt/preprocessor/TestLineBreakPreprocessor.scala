package org.clulab.pdf2txt.preprocessor

import org.clulab.pdf2txt.common.utils.{Test, TextRange}
import org.clulab.pdf2txt.languageModel.{AlwaysLanguageModel, LocalSetLanguageModel, NeverLanguageModel}

class TestLineBreakPreprocessor extends Test {

  behavior of "LineBreakPreprocessor with AlwaysLanguageModel"

  val preprocessorAlways = new LineBreakPreprocessor(new AlwaysLanguageModel())

  def testAlways(inputText: String, expectedOutputText: String): Unit = {
    it should s"convert ${escape(inputText)}" in {
      val actualOutputText = preprocessorAlways.preprocess(inputText).toString

      actualOutputText shouldBe expectedOutputText
    }
  }

  testAlways("one two three", "one two three")
  testAlways("- two three", "- two three")
  testAlways("- two -", "- two -")

  testAlways("one\ntwo\nthree", "one\ntwo\nthree")
  testAlways("-\ntwo\nthree", "-\ntwo\nthree")
  testAlways("-\ntwo\n-", "-\ntwo\n-")

  testAlways("-one\ntwo\nthree", "-one\ntwo\nthree")
  testAlways("one-\ntwo\nthree", "onetwo\nthree")
  testAlways("one-\ntwo\n-", "onetwo\n-")

  testAlways("one-\ntwo-\nthree", "onetwothree")

  testAlways("A pre-\nhensile tail is un-\ncommon.", "A prehensile tail is uncommon.")
  testAlways("A pre-\r\nhensile tail is un-\r\ncommon.", "A prehensile tail is uncommon.")

  behavior of "LineBreakPreprocessor with NeverLanguageModel"

  val preprocessorNever = new LineBreakPreprocessor(new NeverLanguageModel())

  def testNever(inputText: String, expectedOutputText: String): Unit = {
    it should s"convert ${escape(inputText)}" in {
      val actualOutputText = preprocessorNever.preprocess(inputText).toString

      actualOutputText shouldBe expectedOutputText
    }
  }

  testNever("abc-\nxyz", "abc-\nxyz")
  // Some depend on the language model from the local document.
  testNever("abcxyz abc-\nxyz", "abcxyz abcxyz")
  testNever("abc-\nxyz abcxyz", "abcxyz abcxyz")
  testNever("abcAxyz abc-\nxyz", "abcAxyz abc-\nxyz")
}
