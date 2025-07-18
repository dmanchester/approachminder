package com.dmanchester.approachminder

import com.dmanchester.approachminder.Utils.interpolateScalar
import com.dmanchester.approachminder.typeswithbehavior.{Airport, ContinuouslyNearingTrajectory, Trajectory}
import com.dmanchester.approachminder.typeswithoutbehavior.{HasLongLatAlt, LongLatAlt, RunwayAndReferencePoint}

case class ApproachAndLanding[+P <: HasLongLatAlt] private(trajectory: ContinuouslyNearingTrajectory[P], runway: Airport#RunwaySurface#Runway, crossingPointInterpolated: HasLongLatAlt)

object ApproachAndLanding {

  /**
   * Tests whether:
   *
   *   - the specified segment of the full trajectory crosses the threshold of the runway in the inbound direction; and
   *   - the segment's endpoint is on the runway surface.
   *
   * If both of those criteria are met, seeks to construct an ApproachAndLanding. The ApproachAndLanding contains the
   * longest-possible subtrajectory that continuously nears the reference point, contains the specified segment, and
   * ends within the runway surface.
   *
   * The ApproachAndLanding also includes the interpolated point (with altitude information) where the specified
   * segment crossed the threshold.
   *
   * While the above process for producing an ApproachAndLanding is generally expected to be reliable, it may create an
   * ApproachAndLanding in the following other cases:
   *
   *   - An aircraft crosses a runway's threshold at the start of its takeoff roll.
   *   - An aircraft executes a go-around over a runway's surface.
   *   - An aircraft overflies a runway at altitude.
   *
   * Such cases should be identified and filtered out separately.
   *
   * @param fullTrajectory The full trajectory.
   * @param segmentIndex The segment of fullTrajectory to test.
   * @param runwayAndReferencePoint The runway to test against, and the reference point for continuously-nearing
   *                                calculations.
   * @tparam P The type of fullTrajectory's positions. (Will also be the type of the ContinuouslyNearingTrajectory.)
   * @return The ApproachAndLanding, along with the count of segments after the specified segment included in the
   *         subtrajectory, wrapped in a `Some`. Or, `None` if at least one of the above criteria wasn't fulfilled, or
   *         if a trajectory that continuously nears the reference point couldn't be constructed.
   */
  def newOption[P <: HasLongLatAlt](fullTrajectory: Trajectory[P], segmentIndex: Int, runwayAndReferencePoint: RunwayAndReferencePoint): Option[(ApproachAndLanding[P], Int)] = {

    val segment = fullTrajectory.segments(segmentIndex)
    val runway = runwayAndReferencePoint.runway

    val inboundCrossingPoint = runway.testForInboundThresholdCrossing(segment)

    for {
      (crossingPoint2D, percentageFromSegStartToSegEnd) <- inboundCrossingPoint
      truncatedTrajectory = fullTrajectory.truncateWhere(segmentIndex + 1, !runway.surface.contains(_)) // truncated after the specified segment to include only positions on the runway surface
      (continuouslyNearingSubtrajectory, addlSegmentsIncluded) <- ContinuouslyNearingTrajectory.newOption(truncatedTrajectory, segmentIndex, runwayAndReferencePoint.referencePoint, runway.geographicCalculator)
      altitudeMeters = interpolateScalar(segment._1.altitudeMeters, segment._2.altitudeMeters, percentageFromSegStartToSegEnd)
      crossingPoint3D = LongLatAlt(crossingPoint2D.longitude, crossingPoint2D.latitude, altitudeMeters)
    } yield {
      (new ApproachAndLanding(continuouslyNearingSubtrajectory, runway, crossingPoint3D), addlSegmentsIncluded)
    }
  }
}
