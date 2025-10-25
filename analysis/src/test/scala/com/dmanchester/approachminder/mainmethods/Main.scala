package com.dmanchester.approachminder.mainmethods

import com.dmanchester.approachminder.Airports.{oak, sfo}
import com.dmanchester.approachminder.Output.trajectoriesWithModelFitsWrites
import com.dmanchester.approachminder.typeswithbehavior.ApproachModelsTester
import com.dmanchester.approachminder.typeswithoutbehavior.{ModelFitWithDisplayFields, RunwayAndReferencePoint}
import com.dmanchester.approachminder.utils.{ApproachModeling, ApproachesAndLandingsExtraction, MathUtils, TrajectoryExtraction}
import com.typesafe.scalalogging.StrictLogging
import play.api.libs.json.Json

import java.nio.charset.StandardCharsets
import java.nio.file.{Files, Paths}

object Main extends StrictLogging {

  def main(args: Array[String]): Unit = {

    val inputDir = "/home/dan/flight-tracking/opensky-data-as-of--2013-01-12--0029/"
    val filesForModels = "all--2022-11-*.json"
    val filesForTesting = "all--2022-12-0*.json"
    val timeGapSecsForPartitioning = 300
    val approachModelIntervalLengthInMeters = 100
    val maxThresholdDistanceInMeters = 15_000
    val maxNormalizedEuclideanDistance = 5.0
    val outputFile = "/tmp/trajectoriesWithBestFits.json"

    val trajectoriesForModelsUnfiltered = TrajectoryExtraction.openSkyFilesToTrajectories(inputDir, filesForModels, timeGapSecsForPartitioning)
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

    val approachModelsTester = ApproachModelsTester(approachModels, maxThresholdDistanceInMeters)

    val trajectoriesForTestingUnfiltered = TrajectoryExtraction.openSkyFilesToTrajectories(inputDir, filesForTesting, timeGapSecsForPartitioning)
    val trajectoriesForTesting = trajectoriesForTestingUnfiltered.filter(_.isPossiblyFixedWingPowered)
    logger.info(s"${trajectoriesForTesting.length} trajectories for testing (after filtering)")

    val trajectoriesWithBestFits = trajectoriesForTesting.map { trajectory =>

      trajectory.mapPositionsByIndex { case (positions, index) =>

        val currentPosition = positions(index)

        val bestFitWithDisplayFields = if (index == 0) {
          None
        } else {
          val previousPosition = positions(index - 1)
          val bestFit = approachModelsTester.testForBestFit(previousPosition, currentPosition)
          bestFit filter { theFit =>
            theFit.deviation.normalizedEuclideanDistance <= maxNormalizedEuclideanDistance
          } map { theFit =>
            val thresholdDistanceInMeters = theFit.model.runway.distanceInMetersToThresholdCenter(currentPosition)
            val horizontalDevInMeters = MathUtils.isoscelesBaseLength(theFit.deviation.angleDevInDegrees, theFit.distanceTestedAtInMeters.toDouble)
            ModelFitWithDisplayFields(theFit, thresholdDistanceInMeters, horizontalDevInMeters)
          }
        }

        (currentPosition, bestFitWithDisplayFields)
      }
    }

    val trajectoriesWithBestFitsJson = Json.toJson(trajectoriesWithBestFits)(trajectoriesWithModelFitsWrites)
    Files.write(Paths.get(outputFile), trajectoriesWithBestFitsJson.toString().getBytes(StandardCharsets.UTF_8))
    logger.info("Trajectories tested; output file written!")
  }
}
