package com.dmanchester.approachminder

import com.dmanchester.approachminder.MathUtils.interpolateScalar

// TODO Is tempting to make this a case class; but how would we get "equal" to fire reliably, since crossingPointInterpolated is a calculated Double?

class ApproachAndLanding2[A <: HasLongLatAlt] private(val trajectory: ContinuouslyNearingTrajectory2[A], val threshold: Airport#RunwaySurface#RunwayThreshold, val crossingPointInterpolated: HasLongLatAlt, val referencePoint: HasLongLat)

object ApproachAndLanding2 {

  /**
   * Tests whether:
   *
   *   - the specified segment of the full trajectory crosses the threshold in the inbound direction; and
   *   - the segment's endpoint is on the runway surface.
   *
   * If both of those criteria are met, seeks to construct an ApproachAndLanding2. The ApproachAndLanding2 contains the
   * longest-possible subtrajectory that continuously nears the reference point, contains the specified segment, and
   * ends within the runway surface.
   *
   * @param fullTrajectory
   * @param segmentIndex
   * @param threshold
   * @param referencePoint
   * @tparam A
   * @return The ApproachAndLanding2, along with the count of segments after the specified segment contained within the
   *         subtrajectory, wrapped in a `Some`. Or, `None` if at least one of the above criteria wasn't fulfilled, or
   *         if a trajectory that continuously nears the reference point couldn't be constructed.
   */
  def newOption[A <: HasLongLatAlt](fullTrajectory: Trajectory[A], segmentIndex: Int, threshold: Airport#RunwaySurface#RunwayThreshold, referencePoint: HasLongLat): Option[(ApproachAndLanding2[A], Int)] = {

    val positionA = fullTrajectory.positions(segmentIndex)
    val positionB = fullTrajectory.positions(segmentIndex + 1)
    val inboundCrossingPoint = threshold.interpolateInboundCrossingPoint(positionA, positionB)

    for {
      (crossingPoint2D, percentageFromSegStartToSegEnd) <- inboundCrossingPoint
      (positionsBeforeMiddleSegment, positionsAfterMiddleSegment) = fullTrajectory.positions.splitAt(segmentIndex + 1)
      truncatedTrajectoryAsPositions = positionsBeforeMiddleSegment :++ positionsAfterMiddleSegment.takeWhile(threshold.runwaySurface.contains)  // truncated after the specified segment to include only positions on the runway surface
      (continuouslyNearingSegment, segmentsAfterMiddleIncluded) <- ContinuouslyNearingTrajectory2.newOption(truncatedTrajectoryAsPositions, segmentIndex, referencePoint, threshold.geographicCalculator)
    } yield {
      val altitudeMeters = interpolateScalar(positionA.altitudeMeters, positionB.altitudeMeters, percentageFromSegStartToSegEnd)
      val crossingPoint3D = LongLatAlt(crossingPoint2D.longitude, crossingPoint2D.latitude, altitudeMeters)
      (new ApproachAndLanding2(continuouslyNearingSegment, threshold, crossingPoint3D, referencePoint), segmentsAfterMiddleIncluded)
    }
  }
}
