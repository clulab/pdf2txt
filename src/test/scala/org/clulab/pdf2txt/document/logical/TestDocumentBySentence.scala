package org.clulab.pdf2txt.document.logical

import org.clulab.pdf2txt.common.utils.{Test, TextRange, TextRanges}

class TestDocumentBySentence extends Test {

  behavior of "DocumentBySentence"

  val inputText = "This is a paragraph.  It has two sentences.\n\nThis is another paragraph.  It also has two sentences."

  it should "know its content" in {
    val document = DocumentBySentence(None, TextRange(inputText))
    val outputText = document.foldLeft(new StringBuilder()) { case (stringBuilder, char) =>
      stringBuilder += char
    }.toString

    outputText shouldBe inputText
  }

  it should "know its children" in {
    val document = DocumentBySentence(None, TextRange(inputText))
    val children = document.getChildren
    val outputText = children.foldLeft(new StringBuilder()) { case (stringBuilder, textRange) =>
      stringBuilder ++= textRange.toString
    }.toString

    outputText shouldBe inputText
  }

  it should "know its separators and content" in {
    val document = DocumentBySentence(None, TextRange(inputText))
    val textRanges = new TextRanges()

    textRanges += document.preSeparatorOpt
    textRanges ++= document.contents
    textRanges += document.postSeparatorOpt

    val outputText = textRanges.toString

    outputText shouldBe inputText
  }

  it should "know separators and content of all sentences" in {
    val document = DocumentBySentence(None, TextRange(inputText))
    val textRanges = new TextRanges()

    textRanges += document.preSeparatorOpt
    document.contents.foreach { sentence =>
      textRanges += sentence.preSeparatorOpt
      textRanges ++= sentence.contents
      textRanges += sentence.postSeparatorOpt
    }
    textRanges += document.postSeparatorOpt

    val outputText = textRanges.toString

    outputText shouldBe inputText
  }

  it should "know separators and content of all words" in {
    val document = DocumentBySentence(None, TextRange(inputText))
    val textRanges = new TextRanges()

    textRanges += document.preSeparatorOpt
    document.contents.foreach { sentence =>
      textRanges += sentence.preSeparatorOpt
      sentence.contents.foreach { word =>
        textRanges += word.preSeparatorOpt
        textRanges ++= word.contents
        textRanges += word.postSeparatorOpt
      }
      textRanges += sentence.postSeparatorOpt
    }
    textRanges += document.postSeparatorOpt

    val outputText = textRanges.toString

    outputText shouldBe inputText
  }
}

