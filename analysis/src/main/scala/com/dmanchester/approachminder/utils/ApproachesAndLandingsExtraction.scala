package com.dmanchester.approachminder.utils

import com.dmanchester.approachminder.typeswithbehavior.{ApproachAndLanding, Trajectory}
import com.dmanchester.approachminder.typeswithoutbehavior.{HasLongLatAlt, RunwayAndReferencePoint}

import scala.annotation.tailrec

object ApproachesAndLandingsExtraction {
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
   * @param runwaysAndReferencePoints
   * @tparam A
   * @return
   */
  def approachesAndLandings2[A <: HasLongLatAlt](trajectory: Trajectory[A], runwaysAndReferencePoints: Seq[RunwayAndReferencePoint]): Seq[ApproachAndLanding[A]] = {
    doApproachesAndLandings2(trajectory, 0, runwaysAndReferencePoints, Seq.empty)
  }

  @tailrec
  private def doApproachesAndLandings2[A <: HasLongLatAlt](remainingTrajectory: Trajectory[A], segmentIndex: Int, runwaysAndReferencePoints: Seq[RunwayAndReferencePoint], accumulator: Seq[ApproachAndLanding[A]]): Seq[ApproachAndLanding[A]] = {

    // TODO What additional test coverage?

    val approachAndLandingOption = approachAndLanding(remainingTrajectory, segmentIndex, runwaysAndReferencePoints)

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
      doApproachesAndLandings2(updatedRemainingTrajectory, updatedSegmentIndex, runwaysAndReferencePoints, updatedAccumulator)
    }
  }

  private def approachAndLanding[A <: HasLongLatAlt](remainingTrajectory: Trajectory[A], segmentIndex: Int, runwaysAndReferencePoints: Seq[RunwayAndReferencePoint]) = {

    val checkSegment = (runwayAndReferencePoint: RunwayAndReferencePoint) => {
      ApproachAndLanding.newOption(remainingTrajectory, segmentIndex, runwayAndReferencePoint)
    }

    runwaysAndReferencePoints.collectFirst { runwayAndReferencePoint =>

      checkSegment(runwayAndReferencePoint) match {
        case Some(approachAndLanding) => approachAndLanding
      }
    }
  }
}
