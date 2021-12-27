package org.clulab.pdf2txt.common.utils

class PairIndexedSeq[T](indexedSeq: IndexedSeq[T]) extends IndexedSeq[(T, T)] {

  override val length: Int = math.max(indexedSeq.length - 1, 0)

  override def apply(index: Int): (T, T) = (indexedSeq(index), indexedSeq(index + 1))
}

object PairIndexedSeq {
  def apply[T](indexedSeq: IndexedSeq[T]): PairIndexedSeq[T] = new PairIndexedSeq(indexedSeq)
}
