package org.clulab.pdf2txt.common.utils

class TestPairOptIterator extends Test {

  behavior of "empty PairOptIndexedSeq"

  it should "by empty no matter what" in {
    val indexedSeq = IndexedSeq.empty[Int]

    PairOptIndexedSeq(indexedSeq) shouldBe empty
  }

  behavior of "short PairOptIndexedSeq"

  it should "have the correct length" in {
    val indexedSeq = IndexedSeq(0)

    PairOptIndexedSeq(indexedSeq) should have length 2
  }

  it should "have the correct content" in {
    val indexedSeq = IndexedSeq(0)

    PairOptIndexedSeq(indexedSeq).map(value => value) should contain theSameElementsInOrderAs Seq(
      (None,    Some(0)),
      (Some(0), None)
    )
  }

  behavior of "longer PairOptIndexedSeq"

  it should "have the correct length" in {
    val indexedSeq = IndexedSeq(1, 2)

    PairOptIndexedSeq(indexedSeq) should have length 3
  }

  it should "have the correct content" in {
    val indexedSeq = IndexedSeq(1, 2)

    PairOptIndexedSeq(indexedSeq).map(value => value) should contain theSameElementsInOrderAs Seq(
      (None,    Some(1)),
      (Some(1), Some(2)),
      (Some(2), None)
    )
  }

  behavior of "long PairOptIndexedSeq"

  it should "have the correct content" in {
    val indexedSeq = IndexedSeq(1, 2, 3)

    PairOptIndexedSeq(indexedSeq).map(value => value) should contain theSameElementsInOrderAs Seq(
      (None,    Some(1)),
      (Some(1), Some(2)),
      (Some(2), Some(3)),
      (Some(3), None)
    )
  }
}
