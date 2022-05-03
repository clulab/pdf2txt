package org.clulab.pdf2txt.preprocessor

import org.clulab.pdf2txt.common.utils.Test

class CasePreprocessorTest extends Test {
  val preprocessor = new CasePreprocessor()

  case class CaseRestoreTest(reason: String, inputText: String, expectedOutputText: String) {

    def test(): Unit = {
      it should reason in {
        val actualOutputText = preprocessor.preprocess(inputText).toString

        actualOutputText shouldBe expectedOutputText
      }
    }
  }

  behavior of "CasePreprocessor"

  val tests = Array(
    CaseRestoreTest("restore to all uppercase", "Mihai likes ibm.", "Mihai likes IBM."),
    CaseRestoreTest("restore to initial uppercase","Mihai likes google.", "Mihai likes Google."),
    CaseRestoreTest("restore to all lowercase", "Nobody likes SQUASH or Squash.", "Nobody likes squash or squash."),
    CaseRestoreTest("not mess with custom case", "However, sQuash is not bad.", "However, sQuash is not bad."),
    CaseRestoreTest("refrain from capitalizing Roman numerals",
      "Once upon a time there were three bears: i) mama bear, iiiv2) papa bear, and viix3] baby bear.",
      "Once upon a time there were three bears: i) Mama Bear, iiiv2) Papa Bear, and viix3] baby bear."
    ),
    // TODO: This should be I, but that change is suppressed by the Roman numeral exception.
    CaseRestoreTest("preferably not overgeneralize to I", "me, myself, and i are one.", "Me, myself, and i are one.")
  )

  tests.foreach(_.test())

  val combinedTest = {

    def combine(strings: Seq[String]): String = strings.mkString("", "\n", "\n")

    val inputText = combine(tests.map(_.inputText))
    val expectedOutputText = combine(tests.map(_.expectedOutputText))

    CaseRestoreTest("restore multiple sentences", inputText, expectedOutputText)
  }

  combinedTest.test()
}
