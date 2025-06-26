package com.dmanchester.approachminder

import com.dmanchester.approachminder.simpletypes.PositionSource

/**
 * An OpenSky position report with only the dynamic fields (i.e., those that one would expect to vary in a trajectory
 * from one report to the next).
 */
trait OpenSkyPositionReport extends HasPositionReportIdentifiers {
  def timePosition: BigInt
  def lastContact: BigInt
  def longitude: BigDecimal
  def latitude: BigDecimal
  def baroAltitude: Option[BigDecimal]
  def onGround: Boolean
  def velocity: Option[BigDecimal]
  def trueTrack: Option[BigDecimal]
  def verticalRate: Option[BigDecimal]
  def geoAltitude: BigDecimal
  def squawk: Option[String]
  def spi: Boolean
  def positionSource: PositionSource
}
