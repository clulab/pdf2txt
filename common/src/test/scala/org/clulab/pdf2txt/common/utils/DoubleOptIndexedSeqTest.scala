package org.clulab.pdf2txt.common.utils

class DoubleOptIteratorTest extends Test {

  behavior of "empty DoubleOptIndexedSeq"

  it should "by empty no matter what" in {
    val indexedSeq = IndexedSeq.empty[Int]

    DoubleOptIndexedSeq(indexedSeq) shouldBe empty
  }

  behavior of "short DoubleOptIndexedSeq"

  it should "have the correct length" in {
    val indexedSeq = IndexedSeq(0)

    DoubleOptIndexedSeq(indexedSeq) should have length 2
  }

  it should "have the correct content" in {
    val indexedSeq = IndexedSeq(0)

    DoubleOptIndexedSeq(indexedSeq).map(value => value) should contain theSameElementsInOrderAs Seq(
      (None,    Some(0)),
      (Some(0), None)
    )
  }

  behavior of "longer DoubleOptIndexedSeq"

  it should "have the correct length" in {
    val indexedSeq = IndexedSeq(1, 2)

    DoubleOptIndexedSeq(indexedSeq) should have length 3
  }

  it should "have the correct content" in {
    val indexedSeq = IndexedSeq(1, 2)

    DoubleOptIndexedSeq(indexedSeq).map(value => value) should contain theSameElementsInOrderAs Seq(
      (None,    Some(1)),
      (Some(1), Some(2)),
      (Some(2), None)
    )
  }

  behavior of "long DoubleOptIndexedSeq"

  it should "have the correct content" in {
    val indexedSeq = IndexedSeq(1, 2, 3)

    DoubleOptIndexedSeq(indexedSeq).map(value => value) should contain theSameElementsInOrderAs Seq(
      (None,    Some(1)),
      (Some(1), Some(2)),
      (Some(2), Some(3)),
      (Some(3), None)
    )
  }
}
