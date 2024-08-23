package com.dmanchester.approachminder

import scala.collection.BuildFrom

/**
 * Contains a `Seq` of `HasTime` elements. The sequence is guaranteed to be time-ordered (ascending).
 */
case class TimeOrderedData[T <: HasTime, S <: Seq[T]] private (seq: S)(implicit bf: BuildFrom[S, T, S]) // FIXME BuildFrom here?

object TimeOrderedData {

  /**
   * Resolves time conflicts among elements. More specifically, given multiple elements having the same time, picks the
   * element furthest down in `sortedSeq` as the winner and discards the other elements with that time.
   *
   * @param sortedSeq Must be sorted (ascending).
   * @param bf
   * @tparam T
   * @tparam S
   * @return
   */
  private def resolveTimeConflicts[T <: HasTime, S[X] <: Seq[X]](sortedSeq: S[T])(implicit bf: BuildFrom[S[T], T, S[T]]): S[T] = {

    val cleanedSeq = if (sortedSeq.isEmpty) {
      Seq.empty[T]
    } else {

      // Given a sequence of elements from 0 to n, start with n as the first cleaned position. Step in reverse from n-1
      // to 0, adding an element to the cleaned elements unless its time matches that of the previously added element.

      val cleanedSeqInitial = Seq(sortedSeq.last)

      sortedSeq.init.foldRight(cleanedSeqInitial) { case (element, cleanedSeqInProgress) =>
        val lastAddedTimePosition = cleanedSeqInProgress.head.timePosition
        if (element.timePosition == lastAddedTimePosition) {
          cleanedSeqInProgress // don't add element
        } else {
          element +: cleanedSeqInProgress
        }
      }
    }

    cleanedSeq.to(bf.toFactory(sortedSeq)) // FIXME Needed?
  }

  def create[T <: HasTime, S[X] <: Seq[X]](sourceSeq: S[T])(implicit bf: BuildFrom[S[T], T, S[T]]): TimeOrderedData[T, S[T]] = {
    val sortedSeq = sourceSeq.sortBy(_.timePosition).to(bf.toFactory(sourceSeq))  // FIXME Try to ditch this!
    val cleanedSeq = resolveTimeConflicts(sortedSeq)
    val cleanedSeqAsS = cleanedSeq.to(bf.toFactory(sourceSeq))
    new TimeOrderedData(cleanedSeqAsS)
  }
}