package com.dmanchester.approachminder

import com.dmanchester.approachminder.Utils.interpolateScalar

import scala.annotation.tailrec

object ExtractionAndEstimation {

  /**
   * Determine the cases of an approach and landing contained within a trajectory.
   *
   * For the criteria for what constitutes an approach and landing, please see the documentation of
   * ApproachAndLanding2.newOption.
   *
   * As its return type suggests, this method can handle trajectories that include multiple  approaches and landings;
   * for example, a continuous sequence of positions that include a landing at one airport, the subsequent take-off, and
   * a landing at another airport.
   *
   * However, this method should not be used directly with highly discontinuous position data: for example, data that
   * shows an aircraft leaving an area of observation on one day, with no further position reports until the aircraft
   * returns to the area the following day.
   *
   * Pre-processing such data with `segmentIntoTrajectoriesByTime` will generally render it suitable for use with this method.
   *
   * @param aircraftProfile
   * @param trajectory
   * @param thresholdsAndReferencePoints
   * @tparam A
   * @return
   */
  def approachesAndLandings2[A <: HasLongLatAlt](trajectory: Trajectory3[A], thresholdsAndReferencePoints: Seq[ThresholdAndReferencePoint]): Seq[ApproachAndLanding2[A]] = {
    doApproachesAndLandings2(trajectory, 0, thresholdsAndReferencePoints, Seq.empty)
  }

  @tailrec
  private def doApproachesAndLandings2[A <: HasLongLatAlt](remainingTrajectory: Trajectory3[A], segmentIndex: Int, thresholdsAndReferencePoints: Seq[ThresholdAndReferencePoint], accumulator: Seq[ApproachAndLanding2[A]]): Seq[ApproachAndLanding2[A]] = {

    // TODO What additional test coverage?

    val approachAndLandingOption = approachAndLanding(remainingTrajectory, segmentIndex, thresholdsAndReferencePoints)

    val updatedAccumulator = accumulator :++ approachAndLandingOption.map(_._1)

    val updatedRemainingTrajectoryAndSegmentIndexOption = approachAndLandingOption.flatMap { case (_, addlSegmentsIncluded) =>
      remainingTrajectory.drop(segmentIndex + 2 + addlSegmentsIncluded).map { updatedRemainingTrajectory => (updatedRemainingTrajectory, 0) }
    }.orElse {
      Option.when(remainingTrajectory.isSegmentIndexValid(segmentIndex + 1)) {
        (remainingTrajectory, segmentIndex + 1)
      }
    }

    if (updatedRemainingTrajectoryAndSegmentIndexOption.isEmpty) {
      updatedAccumulator
    } else {
      val updatedRemainingTrajectory = updatedRemainingTrajectoryAndSegmentIndexOption.get._1
      val updatedSegmentIndex = updatedRemainingTrajectoryAndSegmentIndexOption.get._2
      doApproachesAndLandings2(updatedRemainingTrajectory, updatedSegmentIndex, thresholdsAndReferencePoints, updatedAccumulator)
    }
  }

  private def approachAndLanding[A <: HasLongLatAlt](remainingTrajectory: Trajectory3[A], segmentIndex: Int, thresholdsAndReferencePoints: Seq[ThresholdAndReferencePoint]) = {

    val checkSegment = (thresholdAndReferencePoint: ThresholdAndReferencePoint) => {
      ApproachAndLanding2.createOption(remainingTrajectory, segmentIndex, thresholdAndReferencePoint)
    }

    thresholdsAndReferencePoints.collectFirst { thresholdAndReferencePoint =>

      checkSegment(thresholdAndReferencePoint) match {
        case Some(approachAndLanding) => approachAndLanding
      }
    }
  }

  /**
   * Not a member of ContinuouslyNearingTrajectory2 because it requires HasLongLatAlt data. (CNT2 only required HasLongLat.)
   * @param sourceTrajectory
   * @param intervalLengthInMeters
   * @return
   */
  def interpolateAtIntervals(sourceTrajectory: ContinuouslyNearingTrajectory2[HasLongLatAlt], intervalLengthInMeters: BigDecimal): Option[DistanceKeyed3DTrajectory] = {

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
        val altitudeMeters = interpolateScalar(remainingSourcePositions(0).altitudeMeters, remainingSourcePositions(1).altitudeMeters, interpolatedPosition.relativePosition)
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
