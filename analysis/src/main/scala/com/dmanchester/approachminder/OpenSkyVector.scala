package com.dmanchester.approachminder

import com.dmanchester.approachminder.complextypes.AircraftCategory
import com.dmanchester.approachminder.simpletypes.PositionSource

/**
 * An OpenSky vector, as provided by their API.
 *
 * Reference: https://openskynetwork.github.io/opensky-api/rest.html
 */
case class OpenSkyVector(
                        icao24: String,
                        callsign: Option[String],
                        originCountry: String,
                        timePosition: Option[BigInt],
                        lastContact: BigInt,
                        longitude: Option[BigDecimal],
                        latitude: Option[BigDecimal],
                        baroAltitude: Option[BigDecimal],
                        onGround: Boolean,
                        velocity: Option[BigDecimal],
                        trueTrack: Option[BigDecimal],
                        verticalRate: Option[BigDecimal],
                        // skip "sensors -- int[] -- IDs of the receivers which contributed to this
                        // state vector. Is null if no filtering for sensor was used in the
                        // request."
                        geoAltitude: Option[BigDecimal],
                        squawk: Option[String],
                        spi: Boolean,
                        positionSource: PositionSource,
                        category: AircraftCategory
                      ) {
  def toPositionReportAllFields: Option[OpenSkyPositionReportAllFields] = {

    this match {
      case OpenSkyVector(icao24, callsign, originCountry, Some(timePosition), lastContact, Some(longitude), Some(latitude), baroAltitude, onGround, velocity, trueTrack, verticalRate, Some(geoAltitude), squawk, spi, positionSource, category) =>
        Some(OpenSkyPositionReportAllFields(icao24, callsign, originCountry, timePosition, lastContact, longitude, latitude, baroAltitude, onGround, velocity, trueTrack, verticalRate, geoAltitude, squawk, spi, positionSource, category))
      case _ => None
    }
  }
}
