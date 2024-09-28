package com.dmanchester.approachminder

case class Trajectory3[P] private (icao24: String, callsign: Option[String], category: Option[AircraftCategory], positions: Seq[P])