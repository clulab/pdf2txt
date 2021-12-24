package org.clulab.pdf2txt.document.physical

import org.clulab.pdf2txt.common.utils.{Test, TextRange}

class TestDocumentByChar extends Test {

  behavior of "DocumentByChar"

  it should "know its content" in {
    val inputText = "This is a test."
    val document = DocumentByChar(None, TextRange(inputText))
    val outputText = document.byChar.foldLeft(new StringBuilder()) { case (stringBuilder, char) =>
      stringBuilder += char
    }.toString

    outputText shouldBe inputText
  }

  it should "have complete children" in {
    val inputText = "This is a test."
    val document = DocumentByChar(None, TextRange(inputText))
    val children = document.getChildren
    val outputText = children.foldLeft(new StringBuilder()) { case (stringBuilder, textRange) =>
      stringBuilder ++= textRange.toString
    }.toString

    outputText shouldBe inputText

  }
}
