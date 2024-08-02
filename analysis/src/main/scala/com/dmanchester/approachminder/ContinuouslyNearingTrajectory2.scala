package com.dmanchester.approachminder

import scala.annotation.tailrec

/**
 * A sequence of at least two positions that, when regarded as a sequence of segments, continuously nears a reference
 * point.
 *
 * @param positions
 * @param referencePoint
 * @param calculator
 * @tparam L
 */
case class ContinuouslyNearingTrajectory2[+L <: HasLongLat](positions: Seq[L], referencePoint: HasLongLat)

object ContinuouslyNearingTrajectory2 {

  // FIXME Do I need to suppress default "apply"? (Make private; use instead of "new"?)

  @tailrec
  private def accumulateSegmentsForward[L <: HasLongLat](remainingPositions: Seq[L], referencePoint: HasLongLat, calculator: GeographicCalculator, accumulator: Seq[L]): Seq[L] = {

    if (remainingPositions.length == 1 || !calculator.continuouslyNears(remainingPositions(0), remainingPositions(1), referencePoint)) {
      accumulator
    } else {
      accumulateSegmentsForward(remainingPositions.tail, referencePoint, calculator, accumulator :+ remainingPositions(1))
    }
  }

  @tailrec
  private def accumulateSegmentsBackward[L <: HasLongLat](remainingPositions: Seq[L], referencePoint: HasLongLat, calculator: GeographicCalculator, accumulator: Seq[L]): Seq[L] = {

    val length = remainingPositions.length

    if (length == 1 || !calculator.continuouslyNears(remainingPositions(length - 2), remainingPositions(length - 1), referencePoint)) {
      accumulator
    } else {
      accumulateSegmentsBackward(remainingPositions.init, referencePoint, calculator, remainingPositions(length - 2) +: accumulator)
      // TODO Change other dropRight(1) refs in codebase to init
    }
  }

  /**
   * From a sequence of positions regarded as a sequence of segments, creates a ContinuouslyNearingTrajectory2 instance
   * with the subsequence of segments that:
   *
   *   - includes a specified segment; and
   *   - continuously nears a reference point.
   *
   * @param positions
   * @param segmentIndex
   * @param referencePoint
   * @param calculator
   * @tparam L
   * @throws java.lang.IndexOutOfBoundsException if segmentIndex < 0 or segmentIndex > (positions.length - 2)
   * @return the ContinuouslyNearingTrajectory2, along with the number of segments included in the trajectory *after*
   *         the specified one, as a `Some`; or, `None` if the sequence's specified segment doesn't continuously near
   *         the reference point
   */
  @throws(classOf[IndexOutOfBoundsException])
  def newOption[L <: HasLongLat](positions: Seq[L], segmentIndex: Int, referencePoint: HasLongLat, calculator: GeographicCalculator): Option[(ContinuouslyNearingTrajectory2[L], Int)] = {

    if (segmentIndex < 0 || segmentIndex > positions.length - 2) {
      throw new IndexOutOfBoundsException(s"middleSegmentIndex is $segmentIndex; must be between 0 and ${positions.length - 2}, inclusive!")
    }

    if (!calculator.continuouslyNears(positions(segmentIndex), positions(segmentIndex + 1), referencePoint)) {
      None
    } else {

      val (sourcePositionsBeforeSegment, sourcePositionsAfterSegment) = positions.splitAt(segmentIndex + 1)

      val positionsBeforeSegment = accumulateSegmentsBackward(sourcePositionsBeforeSegment, referencePoint, calculator, Seq(sourcePositionsBeforeSegment.last))
      val positionsAfterSegment = accumulateSegmentsForward(sourcePositionsAfterSegment, referencePoint, calculator, Seq(sourcePositionsAfterSegment.head))

      val continuouslyNearingTrajectory = new ContinuouslyNearingTrajectory2(positionsBeforeSegment :++ positionsAfterSegment, referencePoint)
      Some(continuouslyNearingTrajectory, positionsAfterSegment.length - 1)
    }
  }
}
