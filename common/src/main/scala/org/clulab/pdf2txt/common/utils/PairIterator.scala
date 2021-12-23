package org.clulab.pdf2txt.common.utils

class PairIterator[T](soloIterable: Iterable[T]) extends Iterator[(T, T)] {
  protected val soloIterator = soloIterable.iterator
  protected var prevOpt =
      if (soloIterator.hasNext) Some(soloIterator.next())
      else None

  def hasNext: Boolean = soloIterator.hasNext

  def next(): (T, T) = {
    val result = (prevOpt.get, soloIterator.next())

    prevOpt = Some(result._2)
    result
  }
}

object PairIterator {
  def apply[T](soloIterable: Iterable[T]): PairIterator[T] = new PairIterator(soloIterable)
}
