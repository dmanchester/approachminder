package com.dmanchester.approachminder

import com.dmanchester.approachminder.MathUtils.interpolateScalar

class ThresholdAndReferencePoint private(val threshold: Airport#RunwaySurface#RunwayThreshold, val referencePoint: HasLongLat) {

  /**
   * Tests whether:
   *
   *   - the specified segment of the trajectory crosses this threshold in the inbound direction; and
   *   - the segment's endpoint is on the runway surface.
   *
   * If both of those criteria are met, seeks to construct an ApproachAndLanding2. The ApproachAndLanding2 contains the
   * longest-possible subtrajectory that continuously nears the reference point, contains the specified segment, and
   * ends within the runway surface.
   *
   * @param trajectory
   * @param segmentIndex
   * @tparam A
   * @return The ApproachAndLanding2, along with the count of segments after the specified segment contained within the
   *         subtrajectory, wrapped in a `Some`. Or, `None` if at least one of the above criteria wasn't fulfilled, or
   *         if a trajectory that continuously nears the reference point couldn't be constructed.
   */
  def approachAndLanding[A <: HasLongLatAlt](trajectory: Trajectory[A], segmentIndex: Int): Option[(ApproachAndLanding2[A], Int)] = {

    val positionA = trajectory.positions(segmentIndex)
    val positionB = trajectory.positions(segmentIndex + 1)
    val inboundCrossingPoint = threshold.interpolateInboundCrossingPoint(positionA, positionB)

    for {
      (crossingPoint2D, percentageFromSegStartToSegEnd) <- inboundCrossingPoint
      (positionsBeforeMiddleSegment, positionsAfterMiddleSegment) = trajectory.positions.splitAt(segmentIndex + 1)
      truncatedTrajectoryAsPositions = positionsBeforeMiddleSegment :++ positionsAfterMiddleSegment.takeWhile(threshold.runwaySurface.contains)  // truncated after the specified segment to include only positions on the runway surface
      (continuouslyNearingSegment, segmentsAfterMiddleIncluded) <- ContinuouslyNearingTrajectory2.newOption(truncatedTrajectoryAsPositions, segmentIndex, referencePoint, threshold.geographicCalculator)
    } yield {
      val altitudeMeters = interpolateScalar(positionA.altitudeMeters, positionB.altitudeMeters, percentageFromSegStartToSegEnd)
      val crossingPoint3D = LongLatAlt(crossingPoint2D.longitude, crossingPoint2D.latitude, altitudeMeters)
      (ApproachAndLanding2(continuouslyNearingSegment, threshold, crossingPoint3D), segmentsAfterMiddleIncluded)
    }
  }
}

object ThresholdAndReferencePoint {
  def apply(threshold: Airport#RunwaySurface#RunwayThreshold, referencePoint: HasLongLat): ThresholdAndReferencePoint = new ThresholdAndReferencePoint(threshold, referencePoint)
}