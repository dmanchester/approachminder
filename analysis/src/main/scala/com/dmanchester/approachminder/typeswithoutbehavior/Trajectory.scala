package com.dmanchester.approachminder.typeswithoutbehavior

/**
 * A trajectory of an aircraft, specified via positions at which it has been observed.
 *
 * Is guaranteed to contain at least two positions.
 *
 * @param positions
 * @param icao24
 * @param callsign
 * @param category
 * @tparam P
 */
case class Trajectory[+P] private(positions: Seq[P], icao24: String, callsign: Option[String], category: Option[AircraftCategory]) {

  /**
   * Whether trajectory's aircraft was possibly fixed-wing and powered.
   *
   * @return
   */
  def isPossiblyFixedWingPowered: Boolean = {
    // `Option.forAll` returns `true` on `None`
    category.forall { theCategory =>
      AircraftCategory.fixedWingPowered.contains(theCategory)
    }
  }

  // TODO Can likely delete.
  def mapPositions[A](f: P => A): Trajectory[A] = {
    new Trajectory(positions.map(f), icao24, callsign, category)
  }

  def drop(n: Int): Option[Trajectory[P]] = Trajectory.createOption(positions.drop(n), icao24, callsign, category)

  def isSegmentIndexValid(index: Int): Boolean = {
    index >= 0 && index <= (positions.length - 2)  // n positions constitute (n - 1) segments; with zero-based indexing, last segment's index is (n - 2)
  }
}

object Trajectory {
  def createOption[P](positions: Seq[P], icao24: String, callsign: Option[String], category: Option[AircraftCategory]): Option[Trajectory[P]] = {
    Option.when(positions.length >= 2)(new Trajectory(positions, icao24, callsign, category))
  }
}
