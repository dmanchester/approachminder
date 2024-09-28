package com.dmanchester.approachminder

case class PositionReportIdentifiers(icao24: String, callsign: Option[String], timePosition: BigInt, category: AircraftCategory) extends HasPositionReportIdentifiers
