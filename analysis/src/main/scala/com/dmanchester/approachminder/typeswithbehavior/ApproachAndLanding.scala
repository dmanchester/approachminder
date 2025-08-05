package com.dmanchester.approachminder.typeswithbehavior

import com.dmanchester.approachminder.typeswithoutbehavior.{HasLongLatAlt, LongLatAlt, RunwayAndReferencePoint}
import com.dmanchester.approachminder.utils.MathUtils

case class ApproachAndLanding[+P <: HasLongLatAlt] private(trajectory: ContinuouslyNearingTrajectory[P], runway: Airport#RunwaySurface#Runway, crossingPointInterpolated: HasLongLatAlt, crossingSegmentIndex: Int, crossingPointSegmentPct: Double) {
  val segmentsBeforeCrossingSegment = crossingSegmentIndex
  val segmentsAfterCrossingSegment = trajectory.segments.length - crossingSegmentIndex - 1
}

object ApproachAndLanding {

  /**
   * Tests whether:
   *
   *   - the specified segment of the source trajectory crosses the threshold of the runway in the inbound direction;
   *     and
   *   - the segment's endpoint is on the runway surface.
   *
   * If both of those criteria are met, seeks to construct an ApproachAndLanding. The ApproachAndLanding contains the
   * longest-possible subtrajectory that continuously nears the reference point, contains the specified segment, and
   * ends within the runway surface.
   *
   * The ApproachAndLanding also includes the interpolated point (with altitude information) where the specified
   * segment crossed the threshold.
   *
   * CAUTION: While the above process for producing an ApproachAndLanding is generally expected to be reliable, it may
   * create an ApproachAndLanding in the following other cases:
   *
   *   - An aircraft crosses a runway's threshold at the start of its takeoff roll.
   *   - An aircraft executes a go-around over a runway's surface.
   *   - An aircraft overflies a runway at altitude.
   *
   * Such cases should be identified and filtered out separately.
   *
   * @param sourceTrajectory The source trajectory.
   * @param segmentIndexSourceTrajectory The segment in sourceTrajectory to test.
   * @param runwayAndReferencePoint The runway to test against, and the reference point for continuously-nearing
   *                                calculations.
   * @tparam P The type of sourceTrajectory's positions. (Will also be the type of the ContinuouslyNearingTrajectory.)
   * @return The ApproachAndLanding, wrapped in a Some. Or, None if at least one of the above criteria wasn't fulfilled,
   *         or if a trajectory that continuously nears the reference point couldn't be constructed.
   */
  def newOption[P <: HasLongLatAlt](sourceTrajectory: Trajectory[P], segmentIndexSourceTrajectory: Int, runwayAndReferencePoint: RunwayAndReferencePoint): Option[ApproachAndLanding[P]] = {

    val segment = sourceTrajectory.segments(segmentIndexSourceTrajectory)
    val runway = runwayAndReferencePoint.runway

    val inboundCrossingPoint = runway.testForInboundThresholdCrossing(segment)

    for {
      (crossingPoint2D, crossingPointSegmentPct) <- inboundCrossingPoint
      truncatedTrajectory = sourceTrajectory.truncateWhere(segmentIndexSourceTrajectory + 1, !runway.surface.contains(_)) // truncated after the specified segment to include only positions on the runway surface
      (continuouslyNearingTrajectory, segmentIndexContinuouslyNearingTrajectory) <- ContinuouslyNearingTrajectory.newOption(truncatedTrajectory, segmentIndexSourceTrajectory, runwayAndReferencePoint.referencePoint, runway.geographicCalculator)

      altitudeMeters = MathUtils.interpolateScalar(segment._1.altitudeMeters, segment._2.altitudeMeters, crossingPointSegmentPct)
      crossingPoint3D = LongLatAlt(crossingPoint2D.longitude, crossingPoint2D.latitude, altitudeMeters)
    } yield {
      new ApproachAndLanding(continuouslyNearingTrajectory, runway, crossingPoint3D, segmentIndexContinuouslyNearingTrajectory, crossingPointSegmentPct)
    }
  }
}
