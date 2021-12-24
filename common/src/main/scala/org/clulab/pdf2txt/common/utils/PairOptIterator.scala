package org.clulab.pdf2txt.common.utils

// The sequence is
// (None, Some(head))           if head = true
// (Some(head), Some(head + 1)) if length > 1
// ...                          if length > 2
// (Some(last - 1), Some(last)) if length > 1
// (Some(last), None)           if last = true
class PairOptIterator[T](soloIterable: Iterable[T], head: Boolean = true, last: Boolean = true) extends Iterator[(Option[T], Option[T])] {
  protected val soloIterator = soloIterable.iterator
  protected var isHead = true
  protected var isLast = false
  protected var prevOpt: Option[T] =
      if (soloIterator.hasNext) Some(soloIterator.next())
      else None

  def hasNext: Boolean =
      head && isHead && prevOpt.isDefined ||
      prevOpt.isDefined && soloIterator.hasNext ||
      last && (isLast || prevOpt.isDefined) // for soloist

  def next(): (Option[T], Option[T]) = {
    val result =
        if (head && isHead && prevOpt.isDefined) {
          val result = (None, prevOpt)
          isHead = false
          isLast = !soloIterator.hasNext
          result
        }
        else if (prevOpt.isDefined && soloIterator.hasNext) {
          val result = (prevOpt, Some(soloIterator.next()))
          isHead = false
          isLast = !soloIterator.hasNext
          result
        }
        else {
          val result = (prevOpt, None)
          isHead = false
          isLast = false
          result
        }

    prevOpt = result._2
    result
  }
}

object PairOptIterator {
  def apply[T](soloIterable: Iterable[T], head: Boolean = true, last: Boolean = true): Iterator[(Option[T], Option[T])] = {
    new PairOptIterator(soloIterable, head, last)
  }
}
