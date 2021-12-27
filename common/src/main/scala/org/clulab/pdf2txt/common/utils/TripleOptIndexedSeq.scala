package org.clulab.pdf2txt.common.utils

// The sequence is
// (None,           None,           Some(head))     if length > 0
// (None,           Some(head),     Some(head + 1)) if length > 1
// (Some(head),     Some(head + 1), Some(head + 2)) if length > 2
// ...                                              if length > 3
// (Some(last - 2), Some(last - 1), Some(last))     if length > 2
// (Some(last - 1), Some(last),     None)           if length > 1
// (Some(last),     None,           None)           if length > 0
class TripleOptIndexedSeq[T](indexedSeq: IndexedSeq[T]) extends IndexedSeq[(Option[T], Option[T], Option[T])] {
  protected val n = 3

  override val length: Int =
      if (indexedSeq.isEmpty) 0
      else ((n - 1) + indexedSeq.length + (n - 1)) - (n - 1)

  override def apply(index: Int): (Option[T], Option[T], Option[T]) =
      (indexedSeq.lift(index - 2), indexedSeq.lift(index -1), indexedSeq.lift(index - 0))
}

object TripleOptIndexedSeq {
  def apply[T](indexedSeq: IndexedSeq[T]): TripleOptIndexedSeq[T] = new TripleOptIndexedSeq(indexedSeq)
}
