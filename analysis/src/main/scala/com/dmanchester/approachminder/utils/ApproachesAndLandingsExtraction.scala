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

    runwaysAndReferencePoints.collectFirst { runwayAndReferencePoint =>
      ApproachAndLanding.newOption(remainingTrajectory, segmentIndex, runwayAndReferencePoint) match {
        case Some(approachAndLanding) => approachAndLanding
      }
    }
  }

  /**
   * Extract one or more instances of ApproachAndLanding from a trajectory. More specifically, test the trajectory's
   * segments against a series of runways and reference points. See if a segment crosses the threshold of a runway in
   * the inbound direction and meets the other criteria detailed in ApproachAndLanding.newOption(). If it does, extract
   * an ApproachAndLanding for that runway.
   *
   * This method does not reuse positions across instances of ApproachAndLanding. Once this method has allocated a
   * segment to an instance, it does not consider that segment's positions for inclusion in other instances.
   *
   * @param trajectory The trajectory.
   * @param runwaysAndReferencePoints The runways and reference points.
   * @tparam P The type of remainingTrajectory's positions.
   * @return The instances of ApproachAndLanding.
   */
  def extract[P <: HasLongLatAlt](trajectory: Trajectory[P], runwaysAndReferencePoints: Iterable[RunwayAndReferencePoint]): Seq[ApproachAndLanding[P]] = {

    @tailrec
    def doExtract(remainingTrajectoryOption: Option[Trajectory[P]], segmentIndex: Int, accumulator: Seq[ApproachAndLanding[P]]): Seq[ApproachAndLanding[P]] = {

      if (remainingTrajectoryOption.isEmpty || segmentIndex >= remainingTrajectoryOption.get.segments.length) {
        accumulator
      } else {

        val remainingTrajectory = remainingTrajectoryOption.get

        val extractOneForSegmentResult: Option[(ApproachAndLanding[P], Int)] = extractOneForSegment(remainingTrajectory, segmentIndex, runwaysAndReferencePoints)

        val (updatedRemainingTrajectoryOption, updatedSegmentIndex, updatedAccumulator) = extractOneForSegmentResult.map { case (approachAndLanding, segmentsIncludedAfterIndex) =>
          (remainingTrajectory.drop(segmentIndex + segmentsIncludedAfterIndex + 2), 0, accumulator :+ approachAndLanding)
        } getOrElse {
          (remainingTrajectoryOption, segmentIndex + 1, accumulator)
        }

        doExtract(updatedRemainingTrajectoryOption, updatedSegmentIndex, updatedAccumulator)
      }
    }

    doExtract(Some(trajectory), 0, Seq.empty[ApproachAndLanding[P]])
  }
}
