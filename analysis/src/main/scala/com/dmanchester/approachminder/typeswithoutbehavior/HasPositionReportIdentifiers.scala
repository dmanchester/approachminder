package com.dmanchester.approachminder.typeswithoutbehavior

trait HasPositionReportIdentifiers extends HasICAO24 with HasTime with HasCallsignAndTime {
  def icao24: String
  def callsign: Option[String]
  def timePosition: BigInt
  def category: AircraftCategory
}
