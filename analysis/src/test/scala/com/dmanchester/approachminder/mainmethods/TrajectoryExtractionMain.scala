package com.dmanchester.approachminder.mainmethods

import com.dmanchester.approachminder.utils.TrajectoryExtraction
import com.typesafe.scalalogging.StrictLogging

object TrajectoryExtractionMain extends StrictLogging {

  def main(args: Array[String]): Unit = {
    val dir = "/home/dan/flight-tracking/opensky-data-as-of--2013-01-12--0029/"
    val glob = "all--2022-11-*.json"
    val timeGapSecsForPartitioning = 300

    val trajectoriesUnfiltered = TrajectoryExtraction.openSkyFilesToTrajectories(dir, glob, timeGapSecsForPartitioning)
    val trajectories = trajectoriesUnfiltered.filter(_.isPossiblyFixedWingPowered)
    logger.info(s"${trajectories.length} trajectories after filtering")
  }
}
