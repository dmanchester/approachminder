package com.dmanchester.approachminder

import com.dmanchester.approachminder.utils.TrajectoryExtraction.openSkyFilesToTrajectories

import java.nio.file.Paths

object ThrowawayOpenSkyFilesToTrajectories {

  def main(args: Array[String]): Unit = {

    println("Starting...")

    val dir = "/home/dan/flight-tracking/opensky-data-as-of--2013-01-12--0029/"
//    val glob = "*.json"
    val glob = "all--2022-11-*.json"

    val trajectories = openSkyFilesToTrajectories(dir, glob, 300)
    println("Done!")
  }
}
