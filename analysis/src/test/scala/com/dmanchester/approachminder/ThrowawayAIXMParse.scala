package com.dmanchester.approachminder

import io.dylemma.spac.xml.JavaxSource

object ThrowawayAIXMParse {

  def main(args: Array[String]): Unit = {

    val (airportHeliports, runways, runwayDirections) = AIXM.parseAptXml("/home/dan/APT_AIXM.xml")
//    val (airportHeliports, runways, runwayDirections) = AIXM.parseAptXml(JavaxSource.fromInputStream { getClass.getResourceAsStream("resources/APT_AIXM_snippet.xml") })

    println(s"${airportHeliports.length} airports, ${runways.length} runways, ${runwayDirections.length} runway directions")
//    println(Seq(airportHeliports, runways, runwayDirections).mkString("\n"))
//    println(s"10 airports/heliports:\n${airportHeliports.slice(1000, 1009).mkString("\n")}")
    val firstRunwayDirectionWithoutRunwayEnd = runwayDirections.indexWhere(_.runwayEnd.isEmpty)
    val countRunwayDirectionsWithoutRunwayEnd = runwayDirections.count(_.runwayEnd.isEmpty)
    println(s"$countRunwayDirectionsWithoutRunwayEnd of ${runwayDirections.length}; first is $firstRunwayDirectionWithoutRunwayEnd")
  }
}
