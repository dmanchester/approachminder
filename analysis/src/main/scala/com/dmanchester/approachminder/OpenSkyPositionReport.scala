package com.dmanchester.approachminder

/**
 * An OpenSky position report. Has the same fields as an `OpenSkyVector` (and is typically derived from one), but
 * requires values for the following fields (`OpenSkyVector` has them as `Option[...]`):
 *
 *   - timePosition
 *   - longitude
 *   - latitude
 *   - geoAltitude
 *
 * By requiring more fully formed data in this way, is better-suited to many applications than `OpenSkyVector`.`
 *
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
case class OpenSkyPositionReport (
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
                      ) extends HasPositionReportIdentifiers
