package org.clulab.pdf2txt.common.utils

class EmptyPairOptIterator[T](soloIterable: Iterable[T]) extends Iterator[(Option[T], Option[T])] {
  protected val soloIterator = soloIterable.iterator

  def hasNext: Boolean = soloIterator.hasNext

  // This should throw an exception, the same one that the soloIterator would throw.
  def next(): (Option[T], Option[T]) = {
    val valueOpt = Some(soloIterator.next())

    (valueOpt, valueOpt)
  }
}

// The sequence is
// (None, Some(head))           if head = true
// (Some(head), Some(head + 1)) if length > 1
// ...                          if length > 2
// (Some(last - 1), Some(last)) if length > 1
// (Some(last), None)           if last = true
class PairOptIterator[T](soloIterable: Iterable[T], head: Boolean = true, last: Boolean = true) extends Iterator[(Option[T], Option[T])] {
  require(soloIterable.nonEmpty)

  protected val soloIterator = soloIterable.iterator
  protected var isHead = true
  protected var isLast = false
  protected var prevOpt: Option[T] = None

  def hasNext: Boolean =
      isHead && head && soloIterator.hasNext ||
      prevOpt.isDefined && soloIterator.hasNext ||
      isLast && last && prevOpt.isDefined

  def next(): (Option[T], Option[T]) = {
    val result =
        if (isHead && head && soloIterator.hasNext) {
          isHead = false
          (None, Some(soloIterator.next()))
        }
        else if (isLast && last && prevOpt.isDefined) {
          isLast = false
          (prevOpt, None)
        }
        else {
          val result = (prevOpt, Some(soloIterator.next()))
          isLast = !soloIterator.hasNext
          result
        }

    prevOpt = result._2
    result
  }
}

object PairOptIterator {
  def apply[T](soloIterable: Iterable[T], head: Boolean = true, last: Boolean = true): Iterator[(Option[T], Option[T])] = {
    if (soloIterable.isEmpty) new EmptyPairOptIterator(soloIterable)
    else new PairOptIterator(soloIterable, head, last)
  }
}
