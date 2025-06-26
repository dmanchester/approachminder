package com.dmanchester.approachminder

import com.dmanchester.approachminder.simpletypes.PositionSource

/**
 * An OpenSky position report with only the dynamic fields (i.e., those that one would expect to vary in a trajectory
 * from one report to the next).
 *
 * TODO Better way to manage these fields as they appear here and in OpenSkyPositionReportAllFields?
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
case class OpenSkyPositionReport(
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
                        positionSource: PositionSource
                      )
