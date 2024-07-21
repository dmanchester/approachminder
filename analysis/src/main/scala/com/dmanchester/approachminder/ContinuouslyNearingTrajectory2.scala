package com.dmanchester.approachminder

import scala.annotation.tailrec

/**
 * A sequence of at least two positions that, when regarding as a sequence of segments, continuously nears a reference
 * point.
 *
 * @param positions
 * @param referencePoint
 * @param calculator
 * @tparam L
 */
class ContinuouslyNearingTrajectory2[+L <: HasLongLat] private(val positions: Seq[L], val referencePoint: HasLongLat, val calculator: GeographicCalculator)

object ContinuouslyNearingTrajectory2 {

  /**
   * From a trajectory, creates a ContinuouslyNearingTrajectory2 instance with the subtrajectory that:
   *
   *   - starts with the trajectory's first segment; and
   *   - continuously nears a reference point.
   *
   * @param trajectory
   * @param referencePoint
   * @param calculator
   * @tparam L
   * @return the subtrajectory as a `Some`; or, `None` if the source trajectory's starting segment doesn't continuously
   *         near the reference point
   */
  def fromStartOfTrajectoryOption[L <: HasLongLat](trajectory: Trajectory[L], referencePoint: HasLongLat, calculator: GeographicCalculator): Option[ContinuouslyNearingTrajectory2[L]] = {
    val subtrajectory = doFromStart(trajectory.positions, referencePoint, calculator, Seq(trajectory.positions.head))

    Option.when(subtrajectory.length >= 2) {
      new ContinuouslyNearingTrajectory2(subtrajectory, referencePoint, calculator)
    }
  }

  @tailrec
  private def doFromStart[L <: HasLongLat](remainingTrajectory: Seq[L], referencePoint: HasLongLat, calculator: GeographicCalculator, resultInProgress: Seq[L]): Seq[L] = {

    if (remainingTrajectory.length == 1 || !calculator.continuouslyNears(remainingTrajectory(0), remainingTrajectory(1), referencePoint)) {
      resultInProgress
    } else {
      doFromStart(remainingTrajectory.tail, referencePoint, calculator, resultInProgress :+ remainingTrajectory(1))
    }
  }

  /**
   * From a trajectory, creates a ContinuouslyNearingTrajectory2 instance with the subtrajectory that:
   *
   *   - continuously nears a reference point; and
   *   - ends with the trajectory's last segment.
   *
   * @param trajectory
   * @param referencePoint
   * @param calculator
   * @tparam L
   * @return the subtrajectory as a `Some`; or, `None` if the source trajectory's ending segment doesn't continuously
   *         near the reference point
   */
  def fromEndOfTrajectoryOption[L <: HasLongLat](trajectory: Trajectory[L], referencePoint: HasLongLat, calculator: GeographicCalculator): Option[ContinuouslyNearingTrajectory2[L]] = {
    val subtrajectory = doFromEnd(trajectory.positions, referencePoint, calculator, Seq(trajectory.positions.last))

    Option.when(subtrajectory.length >= 2) {
      new ContinuouslyNearingTrajectory2(subtrajectory, referencePoint, calculator)
    }
  }

  @tailrec
  private def doFromEnd[L <: HasLongLat](remainingTrajectory: Seq[L], referencePoint: HasLongLat, calculator: GeographicCalculator, resultInProgress: Seq[L]): Seq[L] = {

    val length = remainingTrajectory.length

    if (length == 1 || !calculator.continuouslyNears(remainingTrajectory(length - 2), remainingTrajectory(length - 1), referencePoint)) {
      resultInProgress
    } else {
      doFromEnd(remainingTrajectory.init, referencePoint, calculator, remainingTrajectory(length - 2) +: resultInProgress)
      // TODO Change other dropRight(1) refs in codebase to init
    }
  }

  /**
   * From a trajectory, creates a ContinuouslyNearingTrajectory2 instance with the subtrajectory that:
   *
   *   - includes a specified "middle" segment of the trajectory (i.e., occurs somewhere between the trajectory's start
   *     and end); and
   *   - continuously nears a reference point.
   *
   * @param trajectory
   * @param middleSegmentIndex
   * @param referencePoint
   * @param calculator
   * @tparam L
   * @throws java.lang.IndexOutOfBoundsException if middleSegmentIndex < 0 or middleSegmentIndex > (trajectory.positions.length - 2)
   * @return the subtrajectory, along with the number of segments included in the subtrajectory *after* the specified
   *         one, as a `Some`; or, `None` if the source trajectory's specified segment doesn't continuously near the
   *         reference point
   */
  @throws(classOf[IndexOutOfBoundsException])
  def fromMiddleOfTrajectoryOption[L <: HasLongLat](trajectory: Trajectory[L], middleSegmentIndex: Int, referencePoint: HasLongLat, calculator: GeographicCalculator): Option[(ContinuouslyNearingTrajectory2[L], Int)] = {

    if (middleSegmentIndex < 0 || middleSegmentIndex > trajectory.positions.length - 2) {
      throw new IndexOutOfBoundsException(s"middleSegmentIndex is $middleSegmentIndex; must be between 0 and ${trajectory.positions.length - 2}, inclusive!")
    }

    if (!calculator.continuouslyNears(trajectory.positions(middleSegmentIndex), trajectory.positions(middleSegmentIndex + 1), referencePoint)) {
      None
    } else {

      val (subtrajectoryBeforeMiddleSegment, subtrajectoryAfterMiddleSegment) = trajectory.positions.splitAt(middleSegmentIndex + 1)

      val start = doFromEnd(subtrajectoryBeforeMiddleSegment, referencePoint, calculator, Seq(subtrajectoryBeforeMiddleSegment.last))
      val end = doFromStart(subtrajectoryAfterMiddleSegment, referencePoint, calculator, Seq(subtrajectoryAfterMiddleSegment.head))

      val continuouslyNearingTrajectory = new ContinuouslyNearingTrajectory2(start :++ end, referencePoint, calculator)
      Some(continuouslyNearingTrajectory, end.length - 1)
    }
  }
}
