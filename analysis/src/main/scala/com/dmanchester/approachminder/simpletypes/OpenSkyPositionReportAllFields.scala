package com.dmanchester.approachminder.simpletypes

import com.dmanchester.approachminder.complextypes.AircraftCategory

/**
 * An OpenSky position report with all the fields of an `OpenSkyVector`, but requires values for the following fields
 * (`OpenSkyVector` has them as `Option[...]`):
 *
 *   - timePosition
 *   - longitude
 *   - latitude
 *   - geoAltitude
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
                      ) extends OpenSkyPositionReport

object OpenSkyPositionReportAllFields {

  def fromVector(vector: OpenSkyVector): Option[OpenSkyPositionReportAllFields] = {

    vector match {
      case OpenSkyVector(icao24, callsign, originCountry, Some(timePosition), lastContact, Some(longitude), Some(latitude), baroAltitude, onGround, velocity, trueTrack, verticalRate, Some(geoAltitude), squawk, spi, positionSource, category) =>
        Some(OpenSkyPositionReportAllFields(icao24, callsign, originCountry, timePosition, lastContact, longitude, latitude, baroAltitude, onGround, velocity, trueTrack, verticalRate, geoAltitude, squawk, spi, positionSource, category))
      case _ => None
    }
  }
}