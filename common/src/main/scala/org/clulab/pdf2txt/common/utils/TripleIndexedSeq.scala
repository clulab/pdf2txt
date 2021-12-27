package org.clulab.pdf2txt.common.utils

class TripleIndexedSeq[T](indexedSeq: IndexedSeq[T]) extends IndexedSeq[(T, T, T)] {

  override val length: Int = math.max(indexedSeq.length - 2, 0)

  override def apply(index: Int): (T, T, T) = (indexedSeq(index), indexedSeq(index + 1), indexedSeq(index + 2))
}

object TripleIndexedSeq {
  def apply[T](indexedSeq: IndexedSeq[T]): TripleIndexedSeq[T] = new TripleIndexedSeq(indexedSeq)
}
