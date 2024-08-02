package com.dmanchester.approachminder

// TODO Is tempting to make this a case class; but how would we get "equal" to fire reliably, since crossingPointInterpolated is a calculated Double?

// TODO If I were to take the minimalist thing to the extreme, would I drop threshold from this class? "Calling code already knows"...

class ApproachAndLanding2[A <: HasLongLatAlt] private(val trajectory: ContinuouslyNearingTrajectory2[A], val threshold: Airport#RunwaySurface#RunwayThreshold, val crossingPointInterpolated: HasLongLatAlt)

object ApproachAndLanding2 {
  def apply[A <: HasLongLatAlt](trajectory: ContinuouslyNearingTrajectory2[A], threshold: Airport#RunwaySurface#RunwayThreshold, crossingPointInterpolated: HasLongLatAlt): ApproachAndLanding2[A] = new ApproachAndLanding2(trajectory, threshold, crossingPointInterpolated)
}