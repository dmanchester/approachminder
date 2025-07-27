package com.dmanchester.approachminder.mainmethods

import com.dmanchester.approachminder.Airports.{oak, sfo}
import com.dmanchester.approachminder.typeswithoutbehavior.RunwayAndReferencePoint
import com.dmanchester.approachminder.utils.{ApproachesAndLandingsExtraction, TrajectoryExtraction}
import com.typesafe.scalalogging.StrictLogging

object ApproachesAndLandingsExtractionMain extends StrictLogging {

  def main(args: Array[String]): Unit = {
    val dir = "/home/dan/flight-tracking/opensky-data-as-of--2013-01-12--0029/"
    val glob = "all--2022-11-*.json"
    val timeGapSecsForPartitioning = 300

    val trajectoriesUnfiltered = TrajectoryExtraction.openSkyFilesToTrajectories(dir, glob, timeGapSecsForPartitioning)
    val trajectories = trajectoriesUnfiltered.filter(_.isPossiblyFixedWingPowered)
    logger.info(s"${trajectories.length} trajectories after filtering")

    val runwaysAndReferencePoints = (sfo.runways :++ oak.runways).map { runway =>
      RunwayAndReferencePoint(runway, runway.opposite.thresholdCenter)
    }

    val approachesAndLandings = trajectories.flatMap { trajectory =>
      ApproachesAndLandingsExtraction.extract(trajectory, runwaysAndReferencePoints)
    }

    logger.info(s"${approachesAndLandings.length} approaches and landings:")

    val approachesAndLandingsByRunway = approachesAndLandings.groupBy(_.runway)
    approachesAndLandingsByRunway.foreach { case (runway, approachesAndLandingsThisRunway) =>
      logger.info(s"  ${approachesAndLandingsThisRunway.length} for ${runway.airport.icaoID}'s Runway ${runway.name}")
    }
  }
}
