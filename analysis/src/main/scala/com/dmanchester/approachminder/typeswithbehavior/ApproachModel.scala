package com.dmanchester.approachminder.typeswithbehavior

import com.dmanchester.approachminder.typeswithoutbehavior.{AngleAndAltitude, HasLongLat, HasLongLatAlt}
import com.dmanchester.approachminder.utils.MathUtils
import com.dmanchester.approachminder.utils.TrajectoryUtils.positionsToSegments

/**
 * A model of aircraft as they approach and land on a runway.
 *
 * The core of the model is a mean trajectory of historical approaches and landings.
 *
 * That trajectory is a series of mean positions that near a reference point. The positions are keyed by distance to the
 * reference point. (So, the distances' *descending* order gives the positions' order.)
 *
 * There are guaranteed to be at least two positions.
 *
 * Each position consists of a mean angle and altitude. The angle is a polar angle relative to the reference point.
 *
 * Typically, a reference point is chosen along the runway centerline beyond the threshold (most commonly, the center
 * point of the opposite threshold). While this is not a requirement, a model that relies on a reference point at or
 * before the threshold would be of less use, as it could not represent aircraft travel beyond that point (including the
 * landing itself).
 *
 * This class is geared toward testing segments of arbitrary trajectories against the model and quantifying their
 * deviation from the model. In line with that purpose, a class instance has a range, specified via minimum and maximum
 * distances.
 *
 * Testing segments that lie outside that range leads to OutOfRange and UnderRange results.
 *
 * @param runway The runway.
 * @param referencePoint The reference point.
 * @param meanTrajectory The mean trajectory.
 * @param minDistanceInMeters The minimum distance at which a trajectory segment can be tested.
 * @param maxDistanceInMeters The maximum distance at which a trajectory segment can be tested.
 */
case class ApproachModel private(runway: Airport#RunwaySurface#Runway, referencePoint: HasLongLat, meanTrajectory: Map[BigDecimal, MeanAngleAndAltitude], minDistanceInMeters: BigDecimal, maxDistanceInMeters: BigDecimal) {

  private val trajectoryDistancesInMeters = BoundedBigDecimals(meanTrajectory.keySet, maxDistanceInMeters)
  private val calculator = runway.geographicCalculator

  /**
   * Test a segment of a trajectory against the model and quantify its deviation from the model.
   *
   * @param previousPosition The previous position in the trajectory.
   * @param currentPosition The current position in the trajectory.
   * @return A TestSegmentResult. The "happy" result is WithinRange, which includes the deviation. -- Other possible
   *         results:
   *           - NotContinuouslyNearing: The trajectory does not continuously near the reference point (and is therefore
   *             inconsistent with an approach and landing on the runway).
   *           - OutOfRange: The trajectory's current position is beyond the maximum distance of the model's range.
   *           - UnderRange: The trajectory's current position is under the minimum distance of the model's range.
   *           - InsufficientlyNearing: The trajectory's current position is between the maximum and minimum distances,
   *             but the trajectory is not expected to cross any further distances at which the model has a mean
   *             position. -- This scenario would arise, for example, if a model had mean positions at 200 m and 100 m,
   *             the current position were at 175 m, and the trajectory were estimated to pass no closer than 150 m from
   *             the reference point.
   */
  def testSegment(previousPosition: HasLongLatAlt, currentPosition: HasLongLatAlt): TestSegmentResult = {

    if (calculator.continuouslyNears(previousPosition, currentPosition, referencePoint)) {

      val distanceToRefPointInMeters = BigDecimal.decimal(calculator.distanceInMeters(currentPosition, referencePoint))

      trajectoryDistancesInMeters.valueLessThanOrEqualTo(distanceToRefPointInMeters) match {

        case GreaterThanUpperBound => OutOfRange

        case LessThanLowerBound => UnderRange

        case WithinBounds(distanceToTestAtInMeters) =>

          calculator.pointOnHalflineAtDistance(previousPosition, currentPosition, referencePoint, distanceToTestAtInMeters.toDouble).map { pointToTest =>

            val altitudeToTest = MathUtils.interpolateScalar(previousPosition.altitudeMeters, currentPosition.altitudeMeters, pointToTest.relativePosition)
            val positionToTest = AngleAndAltitude(pointToTest.angle, altitudeToTest)

            val positionToTestAgainst = meanTrajectory(distanceToTestAtInMeters)
            val deviation = positionToTestAgainst.calculateDeviation(positionToTest)

            WithinRange(deviation, distanceToTestAtInMeters)

          } getOrElse {
            InsufficientlyNearing
          }
      }
    } else {
      NotContinuouslyNearing
    }
  }
}

object ApproachModel {

  /**
   * Create an instance, provided meanTrajectory contains at least two positions.
   *
   * Sets the range--the minimum and maximum distances at which a trajectory segment can be tested--as follows:
   *
   *   - minDistanceInMeters: Set to the minimum distance for which meanTrajectory supplies a position.
   *
   *   - maxDistanceInMeters: Set to the maximum distance for which meanTrajectory supplies a position *plus* the
   *     minimum distance interval between any two positions. -- For example, if meanTrajectory supplies positions at
   *     800 m, 500 m, 400 m, and 200 m, maxDistanceInMeters is set to 900 m (800 m plus the 100 m from the 400-to-500 m
   *     interval).
   *
   * @param runway The runway.
   * @param referencePoint The reference point.
   * @param meanTrajectory The mean trajectory.
   * @return An instance wrapped in Some, provided meanTrajectory contains at least two positions. Otherwise, None.
   */
  def newOption(runway: Airport#RunwaySurface#Runway, referencePoint: HasLongLat, meanTrajectory: Map[BigDecimal, MeanAngleAndAltitude]): Option[ApproachModel] = {

    Option.when(meanTrajectory.size >= 2) {

      val theTrajectoryDistancesInMeters = meanTrajectory.keySet

      val minDistanceInMeters = theTrajectoryDistancesInMeters.min

      val distanceIntervalsInMeters = positionsToSegments(theTrajectoryDistancesInMeters.toSeq.sorted) // TODO In light of this use, generalize the "positionsToSegments" name?
      val minDistanceIntervalInMeters = distanceIntervalsInMeters.map(interval => interval._2 - interval._1).min
      val maxDistanceInMeters = theTrajectoryDistancesInMeters.max + minDistanceIntervalInMeters

      new ApproachModel(runway, referencePoint, meanTrajectory, minDistanceInMeters, maxDistanceInMeters)
    }
  }
}

sealed trait TestSegmentResult
case object NotContinuouslyNearing extends TestSegmentResult
case object OutOfRange extends TestSegmentResult
case class WithinRange(deviation: DeviationFromMean, distanceTestedAtInMeters: BigDecimal) extends TestSegmentResult
case object InsufficientlyNearing extends TestSegmentResult
case object UnderRange extends TestSegmentResult
