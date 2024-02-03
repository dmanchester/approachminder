package com.dmanchester.approachminder

import com.dmanchester.approachminder.MathUtils.interpolateScalar
import org.apache.commons.math3.stat.StatUtils

import scala.annotation.tailrec

object ExtractionAndEstimation {

  /**
   * Determine the cases of an approach and landing contained within a trajectory.
   *
   * The criteria for considering an approach and landing to have occurred are:
   *
   *   * the aircraft crosses a runway threshold in the threshold's inbound direction; and
   *   * the aircraft records at least one position within the rectangle of the runway surface
   *     corresponding to the threshold.
   *
   * While these criteria are generally expected to be reliable, they would consider an approach
   * culminating in a go-around *over the runway surface* (i.e., without lateral deviation) to be an
   * approach and landing.
   *
   * They would similarly consider a high-altitude crossing of a threshold to be an approach and
   * landing.
   *
   * TODO Allow caller to inject altitude-based logic to deal with at least the case of
   * high-altitude crossings.
   *
   * As its return type suggests, this method can handle trajectories that include multiple
   * approaches and landings; for example, a continuous sequence of positions that include a landing
   * at one airport, the subsequent take-off, and a landing at another airport.
   *
   * However, this method should not be used directly with highly discontinuous position data: for
   * example, data that shows an aircraft leaving an area of observation on one day, with no further
   * position reports until the aircraft returns to the area the following day.
   *
   * Pre-processing such data with `segmentIntoTrajectoriesByTime` will generally render it suitable
   * for use with this method.
   *
   * @param aircraftProfile
   * @param trajectory
   * @param thresholds The runway thresholds to check for crossings.
   * @return
   */
  def approachesAndLandings[A <: HasLongLatAlt](aircraftProfile: AircraftProfile, trajectory: Seq[A], thresholds: Thresholds): Seq[ApproachAndLanding[A]] = {
    doApproachesAndLandings(aircraftProfile, trajectory, thresholds, 1, 0, Seq.empty[ApproachAndLanding[A]])
  }

  @tailrec private def doApproachesAndLandings[A <: HasLongLatAlt](aircraftProfile: AircraftProfile, trajectory: Seq[A], thresholds: Thresholds, currentIndex: Int, earliestIndexToConsiderForAnApproach: Int, approachesAndLandingsInProgress: Seq[ApproachAndLanding[A]]): Seq[ApproachAndLanding[A]] = {

    if (currentIndex >= trajectory.length) {

      approachesAndLandingsInProgress

    } else {

      val thresholdCrossedInboundAndPointInterpolated = thresholds.findThresholdCrossedInboundAndInterpolatePoint((trajectory(currentIndex - 1), trajectory(currentIndex)))
      // TODO Name trajectory(currentIndex - 1) and trajectory(currentIndex); use again below?

      val (currentIndexUpdated, earliestIndexToConsiderForAnApproachUpdated, approachesAndLandingsInProgressUpdated) = thresholdCrossedInboundAndPointInterpolated.map { case (threshold, crossingPointInterpolated2D, percentageFromSegStartToSegEnd) =>

        val positionsToConsiderForApproach = trajectory.slice(earliestIndexToConsiderForAnApproach, currentIndex)
        val approach = ContinuouslyNearingTrajectory.clip(positionsToConsiderForApproach, threshold.center, threshold.geographicCalculator)

        val altitudeMeters = interpolateScalar(trajectory(currentIndex - 1).altitudeMeters, trajectory(currentIndex).altitudeMeters, percentageFromSegStartToSegEnd)
        val crossingPointInterpolated3D = LongLatAlt(crossingPointInterpolated2D.longitude, crossingPointInterpolated2D.latitude, altitudeMeters)

        val addlPositionsToConsiderForLanding = trajectory.drop(currentIndex + 1)
        val landing = trajectory(currentIndex) +: subtrajectoryWithinPolygon(addlPositionsToConsiderForLanding, threshold.runwaySurface.rectangle, threshold.geographicCalculator)

        val approachAndLanding = ApproachAndLanding(aircraftProfile, threshold, approach, crossingPointInterpolated3D, landing)

        // The first position after the landing is (currentIndex + landing.length). We update the
        // earliest index to consider for an approach (second value below) to that position.
        //
        // We update currentIndex (first value below) to *one more than* that position, since the
        // check for a threshold crossing is from (currentIndex - 1) to currentIndex.
        (currentIndex + landing.length + 1, currentIndex + landing.length, approachesAndLandingsInProgress :+ approachAndLanding)

      } getOrElse {

        (currentIndex + 1, earliestIndexToConsiderForAnApproach, approachesAndLandingsInProgress)

      }

      doApproachesAndLandings(aircraftProfile, trajectory, thresholds, currentIndexUpdated, earliestIndexToConsiderForAnApproachUpdated, approachesAndLandingsInProgressUpdated)
    }
  }

  /**
   * From a trajectory, get the subtrajectory comprising the trajectory's initial points that lie
   * within a polygon.
   *
   * @param trajectory
   * @param polygon
   * @param calculator
   * @return the subtrajectory; or, if `trajectory` is empty, an empty `Seq`.
   */
  def subtrajectoryWithinPolygon[L <: HasLongLat](trajectory: Seq[L], polygon: Polygon, calculator: GeographicCalculator): Seq[L] = {
    trajectory.takeWhile(calculator.contains(polygon, _))
  }

  // TODO Should document why some methods deal in HasLongLat, others in TimeBasedPosition (latter if explicit time dependence, or if returning subset of input; in that second case, maybe switch back to subtyping HasLongLat)?

//  // TODO Need a unit test for toAngles
//  def toAngles(points: SeqWithIndexOffset[HasLongLat], referencePoint: HasLongLat, calculator: GeographicCalculator): SeqWithIndexOffset[Double] = {
//    points.map(calculator.angle(referencePoint, _))
//  }

  /**
   * Interpolate points within a trajectory
   *
   * @param intervalLengthInMeters
   * @return
   */
  def interpolate(trajectory: ContinuouslyNearingTrajectory[HasLongLatAlt], intervalLengthInMeters: BigDecimal): Map[BigDecimal, AngleAndAltitude] = {

    if (trajectory.length < 2) {
      Map.empty[BigDecimal, AngleAndAltitude]
    } else {

      val calculator = trajectory.calculator
      val positions = trajectory.positions
      val referencePoint = trajectory.referencePoint
      val distanceCountdown = BoundedCountdown(calculator.distanceInMeters(positions.head, referencePoint), calculator.distanceInMeters(positions.last, referencePoint), intervalLengthInMeters)
      doInterpolate(trajectory, distanceCountdown, Map.empty[BigDecimal, AngleAndAltitude])
    }
  }

  // TODO Is this "InProgress" naming convention on tailrec methods a good one?

  @tailrec private def doInterpolate(trajectory: ContinuouslyNearingTrajectory[HasLongLatAlt], distancesInMetersToInterpolateAt: BoundedCountdown, interpolatedPointsInProgress: Map[BigDecimal, AngleAndAltitude]): Map[BigDecimal, AngleAndAltitude] = {

    // Conceptually, this method is a "fold" over distancesInMetersToInterpolateAt.currentValueOption.
    // However, Option.fold doesn't mix well with @tailrec. (See https://stackoverflow.com/questions/33567145/scala-tailrec-with-fold
    // and https://stackoverflow.com/questions/70821201/why-cant-option-fold-be-used-tail-recursively-in-scala.)

    if (distancesInMetersToInterpolateAt.currentValueOption.isEmpty) {

      interpolatedPointsInProgress

    } else {

      val distanceInMeters = distancesInMetersToInterpolateAt.currentValueOption.get
      val positions = trajectory.positions
      val interpolatedPointOption = trajectory.calculator.pointOnContinuouslyNearingSegmentAtDistance(positions(0), positions(1), trajectory.referencePoint, distanceInMeters.toDouble).headOption

      val (interpolatedPointsInProgressUpdated, distancesInMetersToInterpolateAtUpdated, trajectoryUpdated) = interpolatedPointOption.map { interpolatedPoint =>
        // Successfully interpolated a point at `distanceInMeters` along the segment from
        // trajectory(0) to trajectory(1).
        val angle = interpolatedPoint.angle
        val altitudeMeters = interpolateScalar(positions(0).altitudeMeters, positions(1).altitudeMeters, interpolatedPoint.relativePosition)
        (interpolatedPointsInProgress + (distanceInMeters -> AngleAndAltitude(angle, altitudeMeters)), distancesInMetersToInterpolateAt.next, trajectory)
      } getOrElse {
        // Was not able to interpolate a point at `distanceInMeters` along the segment from
        // trajectory(0) to trajectory(1). Discard trajectory(0) and try the next segment.
        (interpolatedPointsInProgress, distancesInMetersToInterpolateAt, trajectory.tail)
      }

      doInterpolate(trajectoryUpdated, distancesInMetersToInterpolateAtUpdated, interpolatedPointsInProgressUpdated)
    }
  }

  def meanTrajectory(trajectories: Iterable[Map[BigDecimal, AngleAndAltitude]]): Map[BigDecimal, PositionDistribution] = {

    // Collect the set of distances for which at least one trajectory has a position.
    val distancesInMeters = trajectories.map(_.keys).toSet.flatten

    distancesInMeters.flatMap { thisDistance =>

      val positionsAtThisDistance = trajectories.flatMap(_.get(thisDistance))
      val positionDistributionOption = PositionDistribution.fromDataOption(positionsAtThisDistance)

      // If it was possible to create a PositionDistribution at this distance (generally, that
      // hinges on whether there were at least two positions), queue up a `Map` entry, mapping this
      // distance to that distribution.
      positionDistributionOption.map(thisDistance -> _)

    }.toMap
  }
}
