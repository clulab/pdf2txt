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
    val outputText = document.getChildren.foldLeft(new StringBuilder()) { case (stringBuilder, textRange) =>
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

  behavior of "SentenceDocument"

  it should "know its content" in {
    val document = DocumentBySentence(None, TextRange(inputText))

    document.contents.zipWithIndex.foreach { case (sentence, sentenceIndex) =>
      testSentence(sentence, sentence.toString)
    }
  }

  behavior of "WordDocument"

  it should "know its content" in {
    val document = DocumentBySentence(None, TextRange(inputText))

    document.contents.zipWithIndex.foreach { case (sentence, sentenceIndex) =>
      sentence.contents.zipWithIndex.foreach { case (word, wordIndex) =>
        testWord(word, word.toString)
      }
    }
  }

  def testSentence(sentence: SentenceDocument, inputText: String): Unit = {
    {
      val outputText = sentence.getChildren.foldLeft(new StringBuilder()) { case (stringBuilder, textRange) =>
        stringBuilder ++= textRange.toString
      }.toString

      outputText shouldBe inputText
    }

    {
      val textRanges = new TextRanges()

      textRanges += sentence.preSeparatorOpt
      textRanges ++= sentence.contents
      textRanges += sentence.postSeparatorOpt

      val outputText = textRanges.toString

      outputText shouldBe inputText
    }
  }

  def testWord(word: WordDocument, inputText: String): Unit = {
    {
      val outputText = word.getChildren.foldLeft(new StringBuilder()) { case (stringBuilder, textRange) =>
        stringBuilder ++= textRange.toString
      }.toString

      outputText shouldBe inputText
    }

    {
      val textRanges = new TextRanges()

      textRanges += word.preSeparatorOpt
      textRanges ++= word.contents
      textRanges += word.postSeparatorOpt

      val outputText = textRanges.toString

      outputText shouldBe inputText
    }
  }
}
