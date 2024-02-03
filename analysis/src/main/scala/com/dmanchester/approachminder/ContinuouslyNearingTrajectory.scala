package com.dmanchester.approachminder

import scala.annotation.tailrec

class ContinuouslyNearingTrajectory[+L <: HasLongLat] private(val positions: Seq[L], val referencePoint: HasLongLat, val calculator: GeographicCalculator) {

  def length: Int = positions.length

  def tail: ContinuouslyNearingTrajectory[L] = new ContinuouslyNearingTrajectory[L](positions.tail, referencePoint, calculator)
}

object ContinuouslyNearingTrajectory {

  /**
   * Clip a trajectory to its last point and the contiguous preceding points whose connecting
   * segments continuously near a reference point.
   *
   * For example, given a reference point on a runway threshold and an aircraft trajectory of
   * points #0 - 20, if the aircraft turned toward the runway such that the segments connecting
   * points #10 - 20 continuously neared the runway, this method returns a
   * ContinuouslyNearingTrajectory of those points.
   *
   * @param trajectory
   * @param referencePoint
   * @param calculator
   * @return the subtrajectory; or, if `trajectory` is empty, an empty `Seq`.
   */
  def clip[L <: HasLongLat](trajectory: Seq[L], referencePoint: HasLongLat, calculator: GeographicCalculator): ContinuouslyNearingTrajectory[L] = {

    val clippedTrajectory = if (trajectory.isEmpty) {
      Seq.empty[L]
    } else {

      val trajectoryReverseIterator = trajectory.reverseIterator

      // We process the trajectory in reverse, from latest point to earliest. So, initialize the
      // clipped trajectory with the trajectory's *last* point.
      val clippedTrajectoryInProgress = Seq(trajectoryReverseIterator.next())

      doClip(trajectoryReverseIterator, referencePoint, calculator, clippedTrajectoryInProgress)
    }

    new ContinuouslyNearingTrajectory(clippedTrajectory, referencePoint, calculator)
  }

  @tailrec private def doClip[L <: HasLongLat](trajectoryReverseIterator: Iterator[L], referencePoint: HasLongLat, calculator: GeographicCalculator, clippedTrajectoryInProgress: Seq[L]): Seq[L] = {

    // On each iteration, check whether there's another point available. If there is, check whether
    // the segment connecting that point to the next-later point (i.e., the point most recently
    // added to `clippedTrajectoryInProgress`) continuously nears `referencePoint`.
    //
    // If it does, prepend that point to `clippedTrajectoryInProgress` and continue iterating.
    //
    // If that segment does not continuously near `referencePoint`, stop iterating and return
    // `clippedTrajectoryInProgress` as the completed clipped trajectory.

    if (!trajectoryReverseIterator.hasNext) {

      clippedTrajectoryInProgress

    } else {

      val currentPoint = trajectoryReverseIterator.next()
      val laterPoint = clippedTrajectoryInProgress.head

      if (!calculator.continuouslyNears(currentPoint, laterPoint, referencePoint)) {

        clippedTrajectoryInProgress

      } else {

        val clippedTrajectoryInProgressUpdated = currentPoint +: clippedTrajectoryInProgress

        doClip(trajectoryReverseIterator, referencePoint, calculator, clippedTrajectoryInProgressUpdated)
      }
    }
  }
}