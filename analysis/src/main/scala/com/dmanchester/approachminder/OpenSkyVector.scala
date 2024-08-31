package com.dmanchester.approachminder

// Reference: https://openskynetwork.github.io/opensky-api/rest.html
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
                      )
