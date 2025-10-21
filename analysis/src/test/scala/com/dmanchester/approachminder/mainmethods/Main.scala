package com.dmanchester.approachminder.mainmethods

import com.dmanchester.approachminder.Airports.{oak, sfo}
import com.dmanchester.approachminder.typeswithbehavior.ApproachModels
import com.dmanchester.approachminder.typeswithoutbehavior.RunwayAndReferencePoint
import com.dmanchester.approachminder.utils.{ApproachModeling, ApproachesAndLandingsExtraction, TrajectoryExtraction}
import com.typesafe.scalalogging.StrictLogging

object Main extends StrictLogging {

  def main(args: Array[String]): Unit = {

    val dir = "/home/dan/flight-tracking/opensky-data-as-of--2013-01-12--0029/"
    val filesForModels = "all--2022-11-*.json"
    val filesForTesting = "all--2022-12-*.json"
    val timeGapSecsForPartitioning = 300
    val approachModelIntervalLengthInMeters = 100
    val maxEuclideanDistance = 5.0

    val trajectoriesForModelsUnfiltered = TrajectoryExtraction.openSkyFilesToTrajectories(dir, filesForModels, timeGapSecsForPartitioning)
    val trajectoriesForModels = trajectoriesForModelsUnfiltered.filter(_.isPossiblyFixedWingPowered)
    logger.info(s"${trajectoriesForModels.length} trajectories for models (after filtering)")

    val runwaysAndReferencePoints = (sfo.runways :++ oak.runways).map { runway =>
      RunwayAndReferencePoint(runway, runway.opposite.thresholdCenter)
    }

    val approachesAndLandings = trajectoriesForModels.flatMap { trajectory =>
      ApproachesAndLandingsExtraction.extract(trajectory, runwaysAndReferencePoints)
    }

    logger.info(s"${approachesAndLandings.length} approaches and landings")

    val approachModelsUnsorted = ApproachModeling.constructModels(approachesAndLandings, approachModelIntervalLengthInMeters)
    val approachModels = approachModelsUnsorted.toSeq.sortBy(model => (model.runway.airport.icaoID, model.runway.name)) // Apply a sort to ensure consistent behavior.
    logger.info(s"${approachModels.size} approach models")

    val approachModelsWithTestingWrapper = ApproachModels(approachModels)

    val trajectoriesForTestingUnfiltered = TrajectoryExtraction.openSkyFilesToTrajectories(dir, filesForTesting, timeGapSecsForPartitioning)
    val trajectoriesForTesting = trajectoriesForTestingUnfiltered.filter(_.isPossiblyFixedWingPowered)
    logger.info(s"${trajectoriesForTesting.length} trajectories for testing (after filtering)")

    val trajectoriesWithBestFits = trajectoriesForTesting.map { trajectory =>

      val bestFitsBeforeFiltering = trajectory.segments.flatMap { segment =>
        approachModelsWithTestingWrapper.testForBestFit(segment._1, segment._2)
      }

      // Just because a fit is a "best" fit doesn't mean it's actually a good fit! Discard BestFit instances that are
      // too many standard deviations from the mean.
      val bestFits = bestFitsBeforeFiltering.filterNot(_.deviation.normalizedEuclideanDistance > maxEuclideanDistance)

      (trajectory, bestFits)
    }

    // *** Analysis/testing of the trajectories is complete. ***
    //
    // Code past this point is temporary, to give a perspective on the results.

    // Bubble to the top the trajectories that have the smallest final distanceTestedAtInMeters. These are most likely
    // to be landings.
    val trajectoriesWithLikelyLandingsFirst = trajectoriesWithBestFits.filter(_._2.nonEmpty).sortBy(_._2.last.distanceTestedAtInMeters)

    // Output up to 25 likely landings for each model.
    approachModels foreach { model =>
      logger.info("")
      logger.info(s"${model.runway.airport.icaoID} Runway ${model.runway.name}")

      val trajectoriesThisModel = trajectoriesWithLikelyLandingsFirst.filter(_._2.last.model == model).take(25)
      trajectoriesThisModel.foreach { case (trajectory, bestFits) =>

        val bestFitsFormatted = bestFits.map { fit =>
          val stdDevsFormatted = f"${fit.deviation.normalizedEuclideanDistance}%.2f"
          s"${fit.model.runway.airport.icaoID} ${fit.model.runway.name} @ ${fit.distanceTestedAtInMeters.toInt} m [$stdDevsFormatted]"
        }
        logger.info(s"  ${trajectory.callsign.getOrElse(trajectory.callsign)}:  ${bestFitsFormatted.mkString("  ")}")
      }
    }
  }
}
