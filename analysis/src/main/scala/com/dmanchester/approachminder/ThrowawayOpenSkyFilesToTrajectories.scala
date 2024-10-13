package com.dmanchester.approachminder

import com.dmanchester.approachminder.TrajectoryExtraction.openSkyFilesToTrajectories

import java.nio.file.Paths

object ThrowawayOpenSkyFilesToTrajectories {

  def main(args: Array[String]): Unit = {

    println("Starting...")

    val dir = Paths.get("/home/dan/flight-tracking/opensky-data-as-of--2013-01-12--0029/")
//    val dir = Paths.get("/home/dan/flight-tracking/opensky-data--sfo--as-of-2022-12-02/")
//    val dir = Paths.get("/home/dan/flight-tracking/opensky-data--sfo--as-of-2022-09-01/")
//    val glob = "*.json"
    val glob = "all--2022-11-*.json"

    val jonx = openSkyFilesToTrajectories(dir, glob, 300)
    println("Done!")
  }
}
