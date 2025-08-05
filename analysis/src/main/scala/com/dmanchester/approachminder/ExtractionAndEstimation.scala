package com.dmanchester.approachminder

import com.dmanchester.approachminder.typeswithbehavior.{BoundedCountdown, ContinuouslyNearingTrajectory}
import com.dmanchester.approachminder.typeswithoutbehavior.{HasLongLat, HasLongLatAlt}
import com.dmanchester.approachminder.utils.MathUtils

import scala.annotation.tailrec

object ExtractionAndEstimation {

  /**
   * Not a member of ContinuouslyNearingTrajectory2 because it requires HasLongLatAlt data. (CNT2 only required HasLongLat.)
   * @param sourceTrajectory
   * @param intervalLengthInMeters
   * @return
   */
  def interpolateAtIntervals(sourceTrajectory: ContinuouslyNearingTrajectory[HasLongLatAlt], intervalLengthInMeters: BigDecimal): Option[DistanceKeyed3DTrajectory] = {

    val sourcePositions = sourceTrajectory.positions
    val referencePoint = sourceTrajectory.referencePoint
    val calculator = sourceTrajectory.calculator

    val distanceCountdown = BoundedCountdown(calculator.distanceInMeters(sourcePositions.head, referencePoint), calculator.distanceInMeters(sourcePositions.last, referencePoint), intervalLengthInMeters)

    val targetTrajectoryOption = doInterpolateAtIntervals(sourcePositions, distanceCountdown, referencePoint, calculator: GeographicCalculator, Map.empty[BigDecimal, AngleAndAltitude])
    DistanceKeyed3DTrajectory.newOption(targetTrajectoryOption)
  }

  @tailrec private def doInterpolateAtIntervals(remainingSourcePositions: Seq[HasLongLatAlt], distancesInMetersToInterpolateAt: BoundedCountdown, referencePoint: HasLongLat, calculator: GeographicCalculator, accumulator: Map[BigDecimal, AngleAndAltitude]): Map[BigDecimal, AngleAndAltitude] = {

    // Conceptually, this method is a "fold" over distancesInMetersToInterpolateAt.currentValueOption.
    // However, Option.fold doesn't mix well with @tailrec. (See https://stackoverflow.com/questions/33567145/scala-tailrec-with-fold
    // and https://stackoverflow.com/questions/70821201/why-cant-option-fold-be-used-tail-recursively-in-scala.)

    if (distancesInMetersToInterpolateAt.currentValueOption.isEmpty) {

      accumulator

    } else {

      val distanceInMeters = distancesInMetersToInterpolateAt.currentValueOption.get
      val interpolatedPositionOption = calculator.pointOnContinuouslyNearingSegmentAtDistance(remainingSourcePositions(0), remainingSourcePositions(1), referencePoint, distanceInMeters.toDouble)

      val (updatedAccumulator, updatedDistancesInMetersToInterpolateAt, updatedRemainingSourcePositions) = interpolatedPositionOption.map { interpolatedPosition =>
        // Successfully interpolated a position at `distanceInMeters` along the segment from remainingSourcePositions(0)
        // to remainingSourcePositions(1).
        val angle = interpolatedPosition.angle
        val altitudeMeters = MathUtils.interpolateScalar(remainingSourcePositions(0).altitudeMeters, remainingSourcePositions(1).altitudeMeters, interpolatedPosition.relativePosition)
        (accumulator + (distanceInMeters -> AngleAndAltitude(angle, altitudeMeters)), distancesInMetersToInterpolateAt.next, remainingSourcePositions)
      } getOrElse {
        // Was not able to interpolate a point at `distanceInMeters` along the segment. Discard
        // remainingSourcePositions(0) and try the next segment.
        (accumulator, distancesInMetersToInterpolateAt, remainingSourcePositions.tail)
      }

      doInterpolateAtIntervals(updatedRemainingSourcePositions, updatedDistancesInMetersToInterpolateAt, referencePoint, calculator, updatedAccumulator)
    }
  }

  def meanTrajectory(trajectories: Iterable[Map[BigDecimal, AngleAndAltitude]]): Map[BigDecimal, AngleAndAltitudeWithStats] = {

    // Collect the set of distances for which at least one trajectory has a position.
    val distancesInMeters = trajectories.map(_.keys).toSet.flatten

    distancesInMeters.flatMap { thisDistance =>

      val positionsAtThisDistance = trajectories.flatMap(_.get(thisDistance))
      val angleAndAltitudeWithStatsOption = AngleAndAltitudeWithStats.fromDataOption(positionsAtThisDistance)

      // If it was possible to create an AngleAndAltitudeWithStats at this distance (generally, that
      // hinges on whether there were at least two positions), queue up a `Map` entry, mapping this
      // distance to that distribution.
      angleAndAltitudeWithStatsOption.map(thisDistance -> _)

    }.toMap
  }
}
