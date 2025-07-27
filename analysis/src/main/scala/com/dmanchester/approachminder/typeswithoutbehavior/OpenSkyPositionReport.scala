package com.dmanchester.approachminder.typeswithoutbehavior

/**
 * An OpenSky position report with OpenSkyPositionReportAllFields' dynamic fields only (i.e., those that one would
 * expect to vary in a trajectory from one report to the next).
 *
 * Renames OpenSkyPositionReportAllFields' geoAltitude as "altitudeMeters" and presents both it and longitude and
 * latitude as Double (as opposed to BigDecimal).
 */
case class OpenSkyPositionReport (
  timePosition: BigInt,
  lastContact: BigInt,
  longitude: Double,
  latitude: Double,
  baroAltitude: Option[BigDecimal],
  onGround: Boolean,
  velocity: Option[BigDecimal],
  trueTrack: Option[BigDecimal],
  verticalRate: Option[BigDecimal],
  altitudeMeters: Double,
  squawk: Option[String],
  spi: Boolean,
  positionSource: PositionSource
) extends HasLongLatAlt

object OpenSkyPositionReport {

  def fromPositionReportAllFields(report: OpenSkyPositionReportAllFields): OpenSkyPositionReport = OpenSkyPositionReport(
    report.timePosition,
    report.lastContact,
    report.longitude.toDouble,
    report.latitude.toDouble,
    report.baroAltitude,
    report.onGround,
    report.velocity,
    report.trueTrack,
    report.verticalRate,
    report.geoAltitude.toDouble,
    report.squawk,
    report.spi,
    report.positionSource
  )
}
