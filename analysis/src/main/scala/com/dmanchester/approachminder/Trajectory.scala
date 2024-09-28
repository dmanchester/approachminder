package com.dmanchester.approachminder

/**
 * A trajectory of an aircraft, specified via positions at which it has been observed.
 *
 * Is guaranteed to contain at least two positions.
 *
 * Also contains an `AircraftProfile`.
 *
 * @param aircraftProfile
 * @param positions
 * @tparam A
 */
case class Trajectory[A] private (aircraftProfile: AircraftProfile, positions: Seq[A]) {

  def drop(n: Int): Option[Trajectory[A]] = Trajectory.newOption(aircraftProfile, positions.drop(n))

  def isSegmentIndexValid(index: Int): Boolean = {
    index >= 0 && index <= (positions.length - 2)  // n positions constitute (n - 1) segments; with zero-based indexing, last segment's index is (n - 2)
  }
}

object Trajectory {

  def newOption[A](aircraftProfile: AircraftProfile, positions: Seq[A]): Option[Trajectory[A]] = {
    Option.when(positions.length >= 2)(new Trajectory(aircraftProfile, positions))
  }
}