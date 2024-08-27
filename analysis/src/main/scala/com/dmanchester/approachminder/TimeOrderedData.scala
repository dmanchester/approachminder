package com.dmanchester.approachminder

import scala.collection.BuildFrom

/**
 * Contains a `Seq` of `HasTime` elements. The sequence is guaranteed to be time-ordered (ascending). The sequence is
 * additionally guaranteed to contain at most one element with a given time value.
 */
case class TimeOrderedData[T <: HasTime, S[X] <: Seq[X]] private (seq: S[T])(implicit bf: BuildFrom[S[T], T, S[T]]) {
  // Before needing to add BuildFrom, definition was simply:
  //
  // case class TimeOrderedData[S <: Seq[_ <: HasTime]] private (seq: S) { ... }

  def splitOnGaps(gapTime: BigInt): Seq[TimeOrderedData[T, S]] = {

    if (seq.isEmpty) {
      Seq(this)
    } else {

      val subseqInProgressInitial = Seq(seq.head)
      val completedSubseqsInitial = Seq.empty[Seq[T]]
      val initialVals = (subseqInProgressInitial, completedSubseqsInitial)

      val (subseqInProgressMedial, completedSubseqsMedial) = seq.tail.foldLeft(initialVals) { case ((subseqInProgress, completedSubseqs), currentElement) =>
        val previousElement = subseqInProgress.last

        if (currentElement.timePosition - previousElement.timePosition >= gapTime) {
          // Gap is sufficiently large. Complete subsequence in progress and start a new one with current element.
          (Seq(currentElement), completedSubseqs :+ subseqInProgress)
        } else {
          // Gap is small. Append current element to subsequence in progess.
          (subseqInProgress :+ currentElement, completedSubseqs)
        }
      }

      val completedSubseqsFinal = completedSubseqsMedial :+ subseqInProgressMedial

      completedSubseqsFinal.map { subseq =>
        val subseqAsTypeS = subseq.to(bf.toFactory(seq))
        new TimeOrderedData(subseqAsTypeS)
      }
    }
  }
}

object TimeOrderedData {

  /**
   * Resolves time conflicts among elements. More specifically, given multiple elements having the same time, picks the
   * element furthest down in `sortedSeq` as the winner and discards the other elements with that time.
   *
   * @param sortedSeq Must be sorted (ascending).
   * @tparam T
   * @return
   */
  private def resolveTimeConflicts[T <: HasTime](sortedSeq: Seq[T]): Seq[T] = {

    if (sortedSeq.isEmpty) {
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
  }

  def create[T <: HasTime, S[X] <: Seq[X]](sourceSeq: S[T])(implicit bf: BuildFrom[S[T], T, S[T]]): TimeOrderedData[T, S] = {
    val sortedSeq = sourceSeq.sortBy(_.timePosition)
    val cleanedSeq = resolveTimeConflicts(sortedSeq)
    val cleanedSeqAsTypeS = cleanedSeq.to(bf.toFactory(sourceSeq))
    new TimeOrderedData(cleanedSeqAsTypeS)
  }
}