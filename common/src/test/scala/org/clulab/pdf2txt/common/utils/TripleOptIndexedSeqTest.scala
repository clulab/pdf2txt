package org.clulab.pdf2txt.common.utils

class TripleOptIndexedSeqTest extends Test {

  behavior of "empty TripleOptIndexedSeq"

  it should "by empty no matter what" in {
    val indexedSeq = IndexedSeq.empty[Int]

    TripleOptIndexedSeq(indexedSeq) shouldBe empty
  }

  behavior of "short TripleOptIndexedSeq"

  it should "have the correct length" in {
    val indexedSeq = IndexedSeq(0)

    TripleOptIndexedSeq(indexedSeq) should have length 3
  }

  it should "have the correct content" in {
    val indexedSeq = IndexedSeq(0)

    TripleOptIndexedSeq(indexedSeq).map(value => value) should contain theSameElementsInOrderAs Seq(
      (None,    None,    Some(0)),
      (None,    Some(0), None),
      (Some(0), None,    None)
    )
  }

  behavior of "longer TripleOptIndexedSeq"

  it should "have the correct length" in {
    val indexedSeq = IndexedSeq(1, 2)

    TripleOptIndexedSeq(indexedSeq) should have length 4
  }

  it should "have the correct content" in {
    val indexedSeq = IndexedSeq(1, 2)

    TripleOptIndexedSeq(indexedSeq).map(value => value) should contain theSameElementsInOrderAs Seq(
      (None,    None,    Some(1)),
      (None,    Some(1), Some(2)),
      (Some(1), Some(2), None),
      (Some(2), None,    None)
    )
  }

  behavior of "long TripleOptIndexedSeq"

  it should "have the correct content" in {
    val indexedSeq = IndexedSeq(1, 2, 3)

    TripleOptIndexedSeq(indexedSeq).map(value => value) should contain theSameElementsInOrderAs Seq(
      (None,    None,    Some(1)),
      (None,    Some(1), Some(2)),
      (Some(1), Some(2), Some(3)),
      (Some(2), Some(3), None),
      (Some(3), None,    None)
    )
  }

  behavior of "longest TripleOptIndexedSeq"

  it should "have the correct content" in {
    val indexedSeq = IndexedSeq(1, 2, 3, 4)

    TripleOptIndexedSeq(indexedSeq).map(value => value) should contain theSameElementsInOrderAs Seq(
      (None,    None,    Some(1)),
      (None,    Some(1), Some(2)),
      (Some(1), Some(2), Some(3)),
      (Some(2), Some(3), Some(4)),
      (Some(3), Some(4), None),
      (Some(4), None,    None)
    )
  }
}
