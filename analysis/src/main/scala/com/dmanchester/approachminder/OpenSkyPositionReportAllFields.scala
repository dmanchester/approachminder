package com.dmanchester.approachminder

/**
 * An OpenSky position report with all the fields of an `OpenSkyVector`, but requires values for the following fields
 * (`OpenSkyVector` has them as `Option[...]`):
 *
 *   - timePosition
 *   - longitude
 *   - latitude
 *   - geoAltitude
 *
 * @param icao24
 * @param callsign
 * @param originCountry
 * @param timePosition
 * @param lastContact
 * @param longitude
 * @param latitude
 * @param baroAltitude
 * @param onGround
 * @param velocity
 * @param trueTrack
 * @param verticalRate
 * @param geoAltitude
 * @param squawk
 * @param spi
 * @param positionSource
 * @param category
 */
case class OpenSkyPositionReportAllFields(
                        icao24: String,
                        callsign: Option[String],
                        originCountry: String,
                        timePosition: BigInt,
                        lastContact: BigInt,
                        longitude: BigDecimal,
                        latitude: BigDecimal,
                        baroAltitude: Option[BigDecimal],
                        onGround: Boolean,
                        velocity: Option[BigDecimal],
                        trueTrack: Option[BigDecimal],
                        verticalRate: Option[BigDecimal],
                        geoAltitude: BigDecimal,
                        squawk: Option[String],
                        spi: Boolean,
                        positionSource: PositionSource,
                        category: AircraftCategory
                      ) extends HasPositionReportIdentifiers {

  def toPositionReport: OpenSkyPositionReport = {
    OpenSkyPositionReport(
      timePosition,
      lastContact,
      longitude,
      latitude,
      baroAltitude,
      onGround,
      velocity,
      trueTrack,
      verticalRate,
      geoAltitude,
      squawk,
      spi,
      positionSource
    )
  }

}
