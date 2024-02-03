package com.dmanchester.approachminder

sealed abstract class PositionSource(val description: String)

// Data from https://openskynetwork.github.io/opensky-api/rest.html.
case object ADSB extends PositionSource("ADS-B")
case object ASTERIX extends PositionSource("ASTERIX")
case object MLAT extends PositionSource("MLAT")
case object FLARM extends PositionSource("FLARM")

object PositionSource {

  val byId: Map[Int, PositionSource] = Map(
    0 -> ADSB,
    1 -> ASTERIX,
    2 -> MLAT,
    3 -> FLARM
  )
}

