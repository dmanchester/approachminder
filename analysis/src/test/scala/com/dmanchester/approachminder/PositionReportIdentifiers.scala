package com.dmanchester.approachminder

import com.dmanchester.approachminder.typeswithoutbehavior.AircraftCategory

case class PositionReportIdentifiers(icao24: String, callsign: Option[String], timePosition: BigInt, category: AircraftCategory) extends HasPositionReportIdentifiers
