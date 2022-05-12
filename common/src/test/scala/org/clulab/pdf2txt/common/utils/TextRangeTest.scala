package org.clulab.pdf2txt.common.utils

class TextRangeTest extends Test {

  behavior of "TextRange"

  it should "support removeAll" in {
    val textRange = TextRange("a1aa23aaa4a")
    val actualResult = TextRanges(textRange.removeAll(textRange.findAll("a".r))).toString
    val expectedResult = "1234"

    actualResult should be (expectedResult)
  }

  it should "support replaceAll" in {
    val textRange = TextRange("a1aa23aaa4a")
    val actualResult = TextRanges(textRange.replaceAll(textRange.findAll("a".r), TextRange("A"))).toString
    val expectedResult = "A1AA23AAA4A"

    actualResult should be (expectedResult)
  }

  it should "work in the middle of a string" in {
    val textRange = TextRange("[a1aa23aaa4a]").withoutFirst.withoutLast
    val actualResult = TextRanges(textRange.replaceAll(textRange.findAll("a".r), TextRange("AB"))).toString
    val expectedResult = "AB1ABAB23ABABAB4AB"

    actualResult should be (expectedResult)
  }
}
