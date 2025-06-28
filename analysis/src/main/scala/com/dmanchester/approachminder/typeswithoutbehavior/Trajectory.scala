package com.dmanchester.approachminder.typeswithoutbehavior

/**
 * A trajectory of an aircraft, specified via positions at which it has been observed.
 *
 * Is guaranteed to contain at least two positions.
 */
case class Trajectory[+P] private(positions: Seq[P], icao24: String, callsign: Option[String], category: Option[AircraftCategory]) {

  /**
   * Whether trajectory's aircraft is possibly fixed-wing and powered.
   *
   * TODO Add tests.
   */
  def isPossiblyFixedWingPowered: Boolean = {
    // `Option.forAll` returns `true` on `None`.
    category.forall { theCategory =>
      AircraftCategory.fixedWingPowered.contains(theCategory)
    }
  }

  /**
   * Drop positions from the start of the trajectory.
   *
   * TODO Add tests.
   *
   * @param n The number of positions to drop.
   * @return The shortened trajectory, wrapped in `Some`; or `None`, if fewer than two positions remain.
   */
  def drop(n: Int): Option[Trajectory[P]] = Trajectory.createOption(positions.drop(n), icao24, callsign, category)

  /**
   * Whether a given index value in a valid lookup into the trajectory's positions: non-negative (and thus not "too
   * low"), and also not too high.
   *
   * TODO Add tests.
   */
  def isSegmentIndexValid(index: Int): Boolean = {
    index >= 0 && index <= (positions.length - 2)  // n positions constitute (n - 1) segments; with zero-based indexing, last segment's index is (n - 2)
  }
}

object Trajectory {
  /**
   * Create a new trajectory, provided at least two positions are passed.
   */
  def createOption[P](positions: Seq[P], icao24: String, callsign: Option[String], category: Option[AircraftCategory]): Option[Trajectory[P]] = {
    Option.when(positions.length >= 2)(new Trajectory(positions, icao24, callsign, category))
  }
}
