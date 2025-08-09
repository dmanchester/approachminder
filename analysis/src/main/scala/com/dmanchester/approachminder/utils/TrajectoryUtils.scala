package com.dmanchester.approachminder.utils

import com.dmanchester.approachminder.typeswithbehavior.{BoundedCountdown, ContinuouslyNearingTrajectory, Trajectory}
import com.dmanchester.approachminder.{AngleAndAltitude, GeographicCalculator}
import com.dmanchester.approachminder.typeswithoutbehavior.{DistanceKeyedTrajectory, HasLongLat, HasLongLatAlt}

import scala.annotation.tailrec

object TrajectoryUtils {

  def positionsToSegments[P](positions: Seq[P]): Seq[(P, P)] = positions.sliding(2).toSeq.map { segment => (segment(0), segment(1)) }

  @throws(classOf[IndexOutOfBoundsException])
  private def validateSegmentIndex(trajectory: Trajectory[HasLongLat], segmentIndex: Int): Unit = {

    if (segmentIndex < 0 || segmentIndex > trajectory.segments.length - 1) {
      throw new IndexOutOfBoundsException(s"segmentIndex is $segmentIndex; must be between 0 and ${trajectory.segments.length - 1}, inclusive!")
    }
  }

  /**
   * Starting at a given segment of a trajectory, count the number of contiguous segments that continuously near a
   * reference point.
   *
   * This method is not a member of Trajectory because it enforces a more-restrictive type constraint (HasLongLat) than
   * Trajectory itself, and because it requires a GeographicCalculator.
   *
   * @param trajectory The trajectory.
   * @param segmentIndex The 0-indexed segment to start at.
   * @param referencePoint The reference point.
   * @param calculator The GeographicCalculator to use for distance calculations.
   * @throws java.lang.IndexOutOfBoundsException If segmentIndex < 0 or segmentIndex > (segments.length - 1).
   * @return The number of segments counted. 0 if the starting segment itself does not continuously near the reference
   *         point.
   */
  @throws(classOf[IndexOutOfBoundsException])
  def continuouslyNearingSegmentsStartingAt(trajectory: Trajectory[HasLongLat], segmentIndex: Int, referencePoint: HasLongLat, calculator: GeographicCalculator): Int = {

    validateSegmentIndex(trajectory, segmentIndex)

    @tailrec
    def countSegments(currentSegmentIndex: Int, currentCount: Int): Int = {

      // If there are no more segments to count, or if the current segment does not continuously near the reference
      // point, end the recursion.
      if (currentSegmentIndex == trajectory.segments.length || !calculator.continuouslyNears(trajectory.segments(currentSegmentIndex)._1, trajectory.segments(currentSegmentIndex)._2, referencePoint)) {
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
   * @throws java.lang.IndexOutOfBoundsException If segmentIndex < 0 or segmentIndex > (segments.length - 1).
   * @return The number of segments counted. 0 if the ending segment itself does not continuously near the reference
   *         point.
   */
  @throws(classOf[IndexOutOfBoundsException])
  def continuouslyNearingSegmentsEndingAt(trajectory: Trajectory[HasLongLat], segmentIndex: Int, referencePoint: HasLongLat, calculator: GeographicCalculator): Int = {

    validateSegmentIndex(trajectory, segmentIndex)

    @tailrec
    def countSegments(currentSegmentIndex: Int, currentCount: Int): Int = {

      // If there are no more segments to count, or if the current segment does not continuously near the reference
      // point, end the recursion.
      if (currentSegmentIndex == -1 || !calculator.continuouslyNears(trajectory.segments(currentSegmentIndex)._1, trajectory.segments(currentSegmentIndex)._2, referencePoint)) {
        currentCount
      } else {
        countSegments(currentSegmentIndex - 1, currentCount + 1)
      }
    }

    countSegments(segmentIndex, 0)
  }

  /**
   * Interpolate positions along a ContinuouslyNearingTrajectory at fixed intervals from the trajectory's reference
   * point. The intervals' length is specified as an argument.
   *
   * For example, given a ContinuouslyNearingTrajectory that begins 2.3 km from its reference point and ends at 0.7 km,
   * calling this function with an interval length of 500 m would result in positions at 2.0 km, 1.5 km, and 1.0 km.
   *
   * This function is not a member of ContinuouslyNearingTrajectory because it requires HasLongLatAlt data.
   * (ContinuouslyNearingTrajectory only requires HasLongLat.)
   *
   * @param trajectory The trajectory.
   * @param intervalLengthInMeters The length of the intervals, in meters.
   * @return The interpolated positions as a DistanceKeyedTrajectory.
   */
  def interpolateAtIntervals(trajectory: ContinuouslyNearingTrajectory[HasLongLatAlt], intervalLengthInMeters: BigDecimal): DistanceKeyedTrajectory = {

    val referencePoint = trajectory.referencePoint
    val calculator = trajectory.calculator

    @tailrec def doInterpolateAtIntervals(segments: Seq[(HasLongLatAlt, HasLongLatAlt)], distancesInMetersToInterpolateAt: Option[BoundedCountdown], accumulator: Map[BigDecimal, AngleAndAltitude]): Map[BigDecimal, AngleAndAltitude] = {

      distancesInMetersToInterpolateAt match {

        case None => accumulator

        case Some(theDistancesInMeters) =>
          val currentSegment = segments.head
          val interpolatedPosition = calculator.pointOnContinuouslyNearingSegmentAtDistance(currentSegment._1, currentSegment._2, referencePoint, theDistancesInMeters.currentValue.toDouble)

          val (updatedSegments, updatedDistances, updatedAccumulator) = interpolatedPosition.map { theInterpolatedPosition =>
            // Successfully interpolated a position along currentSegment at the distance.
            val angle = theInterpolatedPosition.angle
            val altitudeInMeters = MathUtils.interpolateScalar(currentSegment._1.altitudeMeters, currentSegment._2.altitudeMeters, theInterpolatedPosition.relativePosition)
            (segments, theDistancesInMeters.next, accumulator.updated(theDistancesInMeters.currentValue, AngleAndAltitude(angle, altitudeInMeters)))
          } getOrElse {
            // Was not able to interpolate a position along currentSegment. Move on to the next segment.
            (segments.tail, Some(theDistancesInMeters), accumulator)
          }

          doInterpolateAtIntervals(updatedSegments, updatedDistances, updatedAccumulator)
      }
    }

    val farthestDistanceInMeters = calculator.distanceInMeters(trajectory.positions.head, referencePoint)
    val farthestDistanceInMetersToInterpolateAt = MathUtils.roundDownToNearestMultiple(farthestDistanceInMeters, intervalLengthInMeters)
    val nearestDistanceInMeters = calculator.distanceInMeters(trajectory.positions.last, referencePoint)

    val distancesInMetersToInterpolateAt = BoundedCountdown.newOption(farthestDistanceInMetersToInterpolateAt, nearestDistanceInMeters, intervalLengthInMeters)

    val targetTrajectory = doInterpolateAtIntervals(trajectory.segments, distancesInMetersToInterpolateAt, Map.empty[BigDecimal, AngleAndAltitude])

    DistanceKeyedTrajectory(targetTrajectory)
  }
}
