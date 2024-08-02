package com.dmanchester.approachminder

class ThresholdAndReferencePoint private(val threshold: Airport#RunwaySurface#RunwayThreshold, val referencePoint: HasLongLat) {

  /**
   * Passes through to ApproachAndLanding2.newOption. For more information, see its documentation.
   *
   * @param trajectory
   * @param segmentIndex
   * @tparam A
   * @return
   */
  def approachAndLanding[A <: HasLongLatAlt](trajectory: Trajectory[A], segmentIndex: Int): Option[(ApproachAndLanding2[A], Int)] = {
    ApproachAndLanding2.newOption(trajectory, segmentIndex, threshold, referencePoint)
  }
}

object ThresholdAndReferencePoint {
  def apply(threshold: Airport#RunwaySurface#RunwayThreshold, referencePoint: HasLongLat): ThresholdAndReferencePoint = new ThresholdAndReferencePoint(threshold, referencePoint)
}