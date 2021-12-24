package org.clulab.pdf2txt.common.utils

class TestPairOptIterator extends Test {

  behavior of "empty PairOptIterator"

  it should "by empty no matter what" in {
    val iterable = Seq.empty[Int]

    PairOptIterator(iterable, false, false) shouldBe empty
    PairOptIterator(iterable, false, true)  shouldBe empty
    PairOptIterator(iterable, true,  false) shouldBe empty
    PairOptIterator(iterable, true,  true)  shouldBe empty
  }

  behavior of "short PairOptIterator"

  it should "have the correct length" in {
    val iterable = Seq(0)

    PairOptIterator(iterable, false, false) should have length 0
    PairOptIterator(iterable, false, true)  should have length 1
    PairOptIterator(iterable, true,  false) should have length 1
    PairOptIterator(iterable, true,  true)  should have length 2
  }

  it should "have the correct content" in {
    val iterable = Seq(0)

    PairOptIterator(iterable, false, false) should have length 0
    PairOptIterator(iterable, false, true)  should have length 1
    PairOptIterator(iterable, true,  false) should have length 1
    PairOptIterator(iterable, true,  true)  should have length 2
  }

  behavior of "longer PairOptIterator"

  it should "have the correct length" in {
    val iterable = Seq(1, 2)

    PairOptIterator(iterable, false, false) should have length 1
    PairOptIterator(iterable, false, true)  should have length 2
    PairOptIterator(iterable, true,  false) should have length 2
    PairOptIterator(iterable, true,  true)  should have length 3
  }

  it should "have the correct content" in {
    val iterable = Seq(1, 2)

    PairOptIterator(iterable, false, false) should have length 1
    PairOptIterator(iterable, false, true)  should have length 2
    PairOptIterator(iterable, true,  false) should have length 2
    PairOptIterator(iterable, true,  true)  should have length 3
  }

  behavior of "long PairOptIterator"

  it should "have the correct content" in {
    val iterable = Seq(1, 2, 3)

    PairOptIterator(iterable, false, false) should have length 1
  }
}
