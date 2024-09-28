package com.dmanchester.approachminder

trait HasPositionReportIdentifiers extends HasICAO24 with HasTime {
  def icao24: String
  def callsign: Option[String]
  def timePosition: BigInt
  def category: AircraftCategory
}
