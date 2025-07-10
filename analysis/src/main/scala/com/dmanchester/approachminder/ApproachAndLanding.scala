package com.dmanchester.approachminder

import com.dmanchester.approachminder.Utils.interpolateScalar
import com.dmanchester.approachminder.typeswithbehavior.{ContinuouslyNearingTrajectory, Trajectory}
import com.dmanchester.approachminder.typeswithoutbehavior.{HasLongLatAlt, LongLatAlt}

case class ApproachAndLanding[+P <: HasLongLatAlt] private(trajectory: ContinuouslyNearingTrajectory[P], threshold: Airport#RunwaySurface#RunwayThreshold, crossingPointInterpolated: HasLongLatAlt)

object ApproachAndLanding {

  /**
   * Tests whether:
   *
   *   - the specified segment of the full trajectory crosses the threshold in the inbound direction; and
   *   - the segment's endpoint is on the runway surface.
   *
   * If both of those criteria are met, seeks to construct an ApproachAndLanding. The ApproachAndLanding contains the
   * longest-possible subtrajectory that continuously nears the reference point, contains the specified segment, and
   * ends within the runway surface.
   *
   * The ApproachAndLanding also includes the interpolated point (with altitude information) where the specified
   * segment crossed the threshold.
   *
   * While the above process for producing an ApproachAndLanding is generally expected to be reliable, it would
   * consider an approach culminating in a go-around *over the runway surface* (i.e., without lateral deviation) to be
   * an approach and landing.
   *
   * It would similarly consider a high-altitude crossing of a threshold to be an approach and landing.
   *
   * @param sourceTrajectory
   * @param segmentIndex
   * @param thresholdAndReferencePoint
   * @tparam A
   * @return The ApproachAndLanding, along with the count of segments after the specified segment included in the
   *         subtrajectory, wrapped in a `Some`. Or, `None` if at least one of the above criteria wasn't fulfilled, or
   *         if a trajectory that continuously nears the reference point couldn't be constructed.
   */
  def newOption[A <: HasLongLatAlt](sourceTrajectory: Trajectory[A], segmentIndex: Int, thresholdAndReferencePoint: ThresholdAndReferencePoint): Option[(ApproachAndLanding[A], Int)] = {

    val sourcePositions = sourceTrajectory.positions
    val positionA = sourcePositions(segmentIndex)
    val positionB = sourcePositions(segmentIndex + 1)
    val threshold = thresholdAndReferencePoint.threshold

    val inboundCrossingPoint = threshold.interpolateInboundCrossingPoint(positionA, positionB)

    for {
      (crossingPoint2D, percentageFromSegStartToSegEnd) <- inboundCrossingPoint
      truncatedTrajectory = sourceTrajectory.truncateWhere(segmentIndex + 1, !threshold.runwaySurface.contains(_))  // truncated after the specified segment to include only positions on the runway surface
      (continuouslyNearingSegment, addlSegmentsIncluded) <- ContinuouslyNearingTrajectory.newOption(truncatedTrajectory, segmentIndex, thresholdAndReferencePoint.referencePoint, threshold.geographicCalculator)
      altitudeMeters = interpolateScalar(positionA.altitudeMeters, positionB.altitudeMeters, percentageFromSegStartToSegEnd)
      crossingPoint3D = LongLatAlt(crossingPoint2D.longitude, crossingPoint2D.latitude, altitudeMeters)
    } yield {
      (new ApproachAndLanding(continuouslyNearingSegment, threshold, crossingPoint3D), addlSegmentsIncluded)
    }
  }
}
