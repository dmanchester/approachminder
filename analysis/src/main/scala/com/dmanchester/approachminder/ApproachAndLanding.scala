package com.dmanchester.approachminder

class ApproachAndLanding[A <: HasLongLatAlt] private(val aircraftProfile: AircraftProfile, val threshold: Airport#RunwaySurface#RunwayThreshold, val approach: ContinuouslyNearingTrajectory[A], val crossingPointInterpolated: HasLongLatAlt, val landing: Seq[A])

object ApproachAndLanding {
  def apply[A <: HasLongLatAlt](aircraftProfile: AircraftProfile, threshold: Airport#RunwaySurface#RunwayThreshold, approach: ContinuouslyNearingTrajectory[A], crossingPointInterpolated: HasLongLatAlt, landing: Seq[A]): ApproachAndLanding[A] = new ApproachAndLanding(aircraftProfile, threshold, approach, crossingPointInterpolated, landing)
}
