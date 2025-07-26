package com.dmanchester.approachminder.utils

import com.dmanchester.approachminder.typeswithbehavior.{ApproachAndLanding, Trajectory}
import com.dmanchester.approachminder.typeswithoutbehavior.{HasLongLatAlt, RunwayAndReferencePoint}

import scala.annotation.tailrec

object ApproachesAndLandingsExtraction {

  /**
   * Test the specified segment of the remaining trajectory against a series of runways and reference points. See if it
   * crosses the threshold of a runway in the inbound direction and meets the other criteria detailed in
   * ApproachAndLanding.newOption(). If it does, extract an ApproachAndLanding for that runway.
   *
   * @param remainingTrajectory The remaining trajectory. ("Remaining" is relevant in the context of this object's
   *                            extract() method, which cuts down a trajectory after constructing an ApproachAndLanding
   *                            and seeks opportunities to construct further instances. -- This method could also be
   *                            used for trajectories where there is no concept of "remaining".)
   * @param segmentIndex The 0-indexed segment to test.
   * @param runwaysAndReferencePoints The runways and reference points.
   * @tparam P The type of remainingTrajectory's positions.
   * @return The ApproachAndLanding for the first runway where the criteria are fulfilled, along with the count of
   *         segments after the specified segment included in the ApproachAndLanding's subtrajectory, wrapped in a Some.
   *         Or, None, if the criteria couldn't be fulfilled against any of the runways.
   */
  private def extractOneForSegment[P <: HasLongLatAlt](remainingTrajectory: Trajectory[P], segmentIndex: Int, runwaysAndReferencePoints: Iterable[RunwayAndReferencePoint]): Option[(ApproachAndLanding[P], Int)] = {

    val checkSegment = (runwayAndReferencePoint: RunwayAndReferencePoint) => {
      ApproachAndLanding.newOption(remainingTrajectory, segmentIndex, runwayAndReferencePoint)
    }

    runwaysAndReferencePoints.collectFirst { runwayAndReferencePoint =>

      checkSegment(runwayAndReferencePoint) match {
        case Some(approachAndLanding) => approachAndLanding
      }
    }
  }

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
   * @tparam P
   * @return
   */

  /**
   * Extract one or more instances of ApproachAndLanding from a trajectory. More specifically, test the trajectory's
   * segments against a series of runways and reference points. See if a segment crosses the threshold of a runway in
   * the inbound direction and meets the other criteria detailed in ApproachAndLanding.newOption(). If it does, extract
   * an ApproachAndLanding for that runway.
   *
   * This method does not reuse segments across instances of ApproachAndLanding. Once this method has allocated a
   * segment to one ApproachAndLanding, it does not consider it for inclusion in other instances.
   *
   * @param trajectory The trajectory.
   * @param runwaysAndReferencePoints The runways and reference points.
   * @tparam P The type of remainingTrajectory's positions.
   * @return The instances of ApproachAndLanding.
   */
  def extract[P <: HasLongLatAlt](trajectory: Trajectory[P], runwaysAndReferencePoints: Iterable[RunwayAndReferencePoint]): Seq[ApproachAndLanding[P]] = {

    @tailrec
    def doExtract[P <: HasLongLatAlt](remainingTrajectory: Trajectory[P], segmentIndex: Int, accumulator: Seq[ApproachAndLanding[P]]): Seq[ApproachAndLanding[P]] = {

      // TODO What additional test coverage?

      val approachAndLandingOption = extractOneForSegment(remainingTrajectory, segmentIndex, runwaysAndReferencePoints)

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
        doExtract(updatedRemainingTrajectory, updatedSegmentIndex, updatedAccumulator)
      }
    }

    doExtract(trajectory, 0, Seq.empty)
  }
}
