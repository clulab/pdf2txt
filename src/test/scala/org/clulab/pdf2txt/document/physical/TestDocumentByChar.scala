package org.clulab.pdf2txt.document.physical

import org.clulab.pdf2txt.common.utils.{Test, TextRange, TextRanges}

class TestDocumentByChar extends Test {

  behavior of "DocumentByChar"

  val inputText = "This is a test."

  it should "know its content" in {
    val document = DocumentByChar(None, TextRange(inputText))
    val outputText = document.foldLeft(new StringBuilder()) { case (stringBuilder, char) =>
      stringBuilder += char
    }.toString

    outputText shouldBe inputText
  }

  it should "know its children" in {
    val document = DocumentByChar(None, TextRange(inputText))
    val outputText = document.getChildren.foldLeft(new StringBuilder()) { case (stringBuilder, textRange) =>
      stringBuilder ++= textRange.toString
    }.toString

    outputText shouldBe inputText
  }

  it should "know its separators and content" in {
    val document = DocumentByChar(None, TextRange(inputText))
    val textRanges = new TextRanges()

    textRanges += document.preSeparatorOpt
    textRanges ++= document.contents
    textRanges += document.postSeparatorOpt

    val outputText = textRanges.toString

    outputText shouldBe inputText
  }
}
