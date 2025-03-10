package com.dmanchester.approachminder

import scala.annotation.tailrec

/**
 * An aircraft trajectory that continuously nears a reference point.
 *
 * The trajectory is specified via positions at which the aircraft has been observed.
 *
 * Is guaranteed to contain at least two positions.
 *
 * NOTE: This class does not include the non-position identifiers found in a Trajectory (icao24, callsign, and
 * category), but if a need for them arose, it would be reasonable to add them.
 *
 * @param positions
 * @param referencePoint
 * @param calculator
 * @tparam L
 */
case class ContinuouslyNearingTrajectory2[+L <: HasLongLat] private (positions: Seq[L], referencePoint: HasLongLat, calculator: GeographicCalculator)

object ContinuouslyNearingTrajectory2 {

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
   * From a sequence of positions representing a sequence of segments, creates a ContinuouslyNearingTrajectory2 instance
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
   * @return the ContinuouslyNearingTrajectory2, along with the count of segments after the specified segment included
   *         within the trajectory, as a `Some`; or, `None` if the sequence's specified segment doesn't continuously
   *         near the reference point
   */
  @throws(classOf[IndexOutOfBoundsException])
  def newOption[L <: HasLongLat](positions: Seq[L], segmentIndex: Int, referencePoint: HasLongLat, calculator: GeographicCalculator): Option[(ContinuouslyNearingTrajectory2[L], Int)] = {

    if (segmentIndex < 0 || segmentIndex > positions.length - 2) {
      throw new IndexOutOfBoundsException(s"segmentIndex is $segmentIndex; must be between 0 and ${positions.length - 2}, inclusive!")
    }

    if (!calculator.continuouslyNears(positions(segmentIndex), positions(segmentIndex + 1), referencePoint)) {
      None
    } else {

      val (sourcePositionsBeforeSegment, sourcePositionsAfterSegment) = positions.splitAt(segmentIndex + 1)

      // Following Seqs are *inclusive* of the segment's endpoints.
      val positionsBeforeSegment = accumulateSegmentsBackward(sourcePositionsBeforeSegment, referencePoint, calculator, Seq(sourcePositionsBeforeSegment.last))
      val positionsAfterSegment = accumulateSegmentsForward(sourcePositionsAfterSegment, referencePoint, calculator, Seq(sourcePositionsAfterSegment.head))

      val continuouslyNearingTrajectory = new ContinuouslyNearingTrajectory2(positionsBeforeSegment :++ positionsAfterSegment, referencePoint, calculator)
      Some(continuouslyNearingTrajectory, positionsAfterSegment.length - 1)
    }
  }
}
