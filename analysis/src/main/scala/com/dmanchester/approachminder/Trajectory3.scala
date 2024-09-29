package com.dmanchester.approachminder

case class Trajectory3[P](icao24: String, callsign: Option[String], category: Option[AircraftCategory], positions: Seq[P])