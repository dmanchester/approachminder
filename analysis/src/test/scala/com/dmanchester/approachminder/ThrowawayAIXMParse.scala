package com.dmanchester.approachminder

object ThrowawayAIXMParse {

  def main(args: Array[String]): Unit = {

    val (airportHeliports, runways) = AIXM.parseAptFile("/home/dan/APT_AIXM.xml")

    println(s"Airports/heliports:\n${airportHeliports.take(50).mkString("\n")}")
    println(s"\n\nRunways:\n${runways.take(50).mkString("\n")}")
  }
}
