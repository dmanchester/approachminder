package com.dmanchester.approachminder

object ThrowawayAIXMParse {

  def main(args: Array[String]): Unit = {

    val (airportHeliports, runways, runwayDirections) = AIXM.parseAptFile("/home/dan/APT_AIXM.xml")

    println(s"${airportHeliports.length} airports, ${runways.length} runways, ${runwayDirections.length} runway directions")
    println(s"10 airports/heliports:\n${airportHeliports.slice(1000, 1009).mkString("\n")}")
  }
}
