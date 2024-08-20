package com.dmanchester.approachminder

/**
 * Contains an `IndexedSeq` of `HasTime` elements. The sequence is guaranteed to be time-ordered (ascending). The
 * sequence is additionally guaranteed to contain at most one element with a given time value.
 */
case class TimeOrderedData[T <: HasTime, S] private (val seq: S)(implicit evidence: S <:< IndexedSeq[T])

object TimeOrderedData {

  /**
   * DAN YOU LEFT OFF HERE. CLEAN UP SCALADOC AND VARILABLE NAMES.
   *
   * EVENTUALLY, SEE WHERE WE CAN SIMPLIFY SIGNATURES IF WE DON'T NEED EXPILICIT "S" TYPE.
   *
   * Clean positions with the same time, picking the position furthest down in `positions` as the
   * winner and discarding the others of that time.
   *
   * @param positions The positions. Must be in ascending time order!
   * @return
   */
  private def removeTimeConflictingElements[T <: HasTime, S](seq: S)(implicit evidence: S <:< IndexedSeq[T]): IndexedSeq[T] = {

    if (seq.isEmpty) {
      IndexedSeq.empty[T]
    } else {

      // Given a sequence of positions from 0 to n, start with n as the first cleaned position. Step
      // in reverse from n-1 to 0, adding a position to the cleaned positions unless its time
      // matches that of the previously added position.

      val cleanedPositionsInitial = IndexedSeq(seq.last)

      seq.init.foldRight(cleanedPositionsInitial) { case (position, cleanedPositions) =>
        if (position.timePosition == cleanedPositions.head.timePosition) {
          cleanedPositions // don't add position
        } else {
          position +: cleanedPositions
        }
      }
    }
  }

  def create[T <: HasTime, S](sourceSeq: S)(implicit evidence: S <:< IndexedSeq[T]): TimeOrderedData[T, IndexedSeq[T]] = {
    val sortedSeq = sourceSeq.sortBy(_.timePosition)
    val cleanedSeq = removeTimeConflictingElements(sortedSeq)
    new TimeOrderedData(cleanedSeq)
  }
}