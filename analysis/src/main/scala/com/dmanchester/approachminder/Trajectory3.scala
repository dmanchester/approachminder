package com.dmanchester.approachminder

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
case class Trajectory3[P] private (positions: Seq[P], icao24: String, callsign: Option[String], category: Option[AircraftCategory]) {

  def drop(n: Int): Option[Trajectory3[P]] = Trajectory3.createOption(positions.drop(n), icao24, callsign, category)

  def isSegmentIndexValid(index: Int): Boolean = {
    index >= 0 && index <= (positions.length - 2)  // n positions constitute (n - 1) segments; with zero-based indexing, last segment's index is (n - 2)
  }
}

object Trajectory3 {
  def createOption[P](positions: Seq[P], icao24: String, callsign: Option[String], category: Option[AircraftCategory]): Option[Trajectory3[P]] = {
    Option.when(positions.length >= 2)(new Trajectory3(positions, icao24, callsign, category))
  }
}
