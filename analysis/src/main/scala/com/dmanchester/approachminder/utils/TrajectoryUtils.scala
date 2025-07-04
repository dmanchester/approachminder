package com.dmanchester.approachminder.utils

import com.dmanchester.approachminder.typeswithbehavior.Trajectory
import com.dmanchester.approachminder.{GeographicCalculator, HasLongLat}

import scala.annotation.tailrec

object TrajectoryUtils {

  /**
   * Starting at a given segment of a trajectory, count the number of contiguous segments that continuously near a
   * reference point.
   *
   * This method is not a member of Trajectory because it enforces a more-restrictive type constraint (HasLongLat) than
   * Trajectory itself.
   *
   * @param trajectory The trajectory.
   * @param segmentIndex The 0-indexed segment to start at.
   * @param referencePoint The reference point.
   * @param calculator The GeographicCalculator to use for distance calculations.
   * @return The number of segments counted. 0 if the starting segment itself does not continuously near the reference
   *         point.
   */
  def continuouslyNearingSegmentsStartingAt(trajectory: Trajectory[HasLongLat], segmentIndex: Int, referencePoint: HasLongLat, calculator: GeographicCalculator): Int = {

    @tailrec
    def countSegments(currentSegmentIndex: Int, currentCount: Int): Int = {

      // If there are no more segments to count, or if the current segment does not continuously near the reference
      // point, end the recursion.
      if (currentSegmentIndex >= trajectory.segments.length || !calculator.continuouslyNears(trajectory.segments(currentSegmentIndex)._1, trajectory.segments(currentSegmentIndex)._2, referencePoint)) {
        currentCount
      } else {
        countSegments(currentSegmentIndex + 1, currentCount + 1)
      }
    }

    countSegments(segmentIndex, 0)
  }

  /**
   * Ending at a given segment of a trajectory, count the number of contiguous segments that continuously near a
   * reference point.
   *
   * This method is not a member of Trajectory because it enforces a more-restrictive type constraint (HasLongLat) than
   * Trajectory itself.
   *
   * @param trajectory The trajectory.
   * @param segmentIndex The 0-indexed segment to end at.
   * @param referencePoint The reference point.
   * @param calculator The GeographicCalculator to use for distance calculations.
   * @return The number of segments counted. 0 if the ending segment itself does not continuously near the reference
   *         point.
   */

  def continuouslyNearingSegmentsEndingAt(trajectory: Trajectory[HasLongLat], segmentIndex: Int, referencePoint: HasLongLat, calculator: GeographicCalculator): Int = {

    @tailrec
    def countSegments(currentSegmentIndex: Int, currentCount: Int): Int = {

      // If there are no more segments to count, or if the current segment does not continuously near the reference
      // point, end the recursion.
      if (currentSegmentIndex < 0 || !calculator.continuouslyNears(trajectory.segments(currentSegmentIndex)._1, trajectory.segments(currentSegmentIndex)._2, referencePoint)) {
        currentCount
      } else {
        countSegments(currentSegmentIndex - 1, currentCount + 1)
      }
    }

    countSegments(segmentIndex, 0)
  }
}
