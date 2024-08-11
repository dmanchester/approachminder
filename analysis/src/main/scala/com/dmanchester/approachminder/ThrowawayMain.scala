package com.dmanchester.approachminder

import play.api.libs.json.Json

import java.nio.file.{Files, Path, Paths}
import java.nio.charset.StandardCharsets

object ThrowawayMain {

  def filesToTrajectories(files: Seq[Path]): Seq[(AircraftProfile, Trajectory[TimeBasedPosition])] = {

    val readUniqueVectorsResult = IO.readUniqueVectors(files)
    println(readUniqueVectorsResult)
    println(readUniqueVectorsResult.errors)

    val allPositions = GroupingSortingFiltering.fullySpecifiedPositions(readUniqueVectorsResult.uniqueVectors)
    val positionsByAircraftUnfiltered = GroupingSortingFiltering.positionsByAircraft(allPositions)
    val positionsByAircraft = GroupingSortingFiltering.filterPossiblyFixedWingPowered(positionsByAircraftUnfiltered)

    GroupingSortingFiltering.trajectories(positionsByAircraft, 300)
  }

  def main(args: Array[String]): Unit = {

    println("Starting...")

    val dirPath = Paths.get("/home/dan/flight-tracking/opensky-data-as-of--2013-01-12--0029/")
//    val dirPath = Paths.get("/home/dan/flight-tracking/opensky-data--sfo--as-of-2022-12-02/")
//    val dirPath = Paths.get("/home/dan/flight-tracking/opensky-data--sfo--as-of-2022-09-01/")
//    val glob = "*.json"
    val glob = "all--2022-11-*.json"

    val files = IO.resolveGlob(dirPath, glob)
    val trajectories = filesToTrajectories(files)

    println(s"${trajectories.length} trajectories")

    val thresholds = Thresholds(Airports.sfo.thresholds ++: Airports.oak.thresholds)

    // DAN YOU LEFT OFF HERE 8/9/2024; TO GET STUFF TO COMPILE, COMMENTED OUT THROUGH: "Files.write(Paths.get..."

//    val approachesAndLandings = trajectories.flatMap { case (aircraftProfile, trajectory) =>
//      ExtractionAndEstimation.approachesAndLandings2(aircraftProfile, trajectory, thresholds)
//    }
//
//    println(s"${approachesAndLandings.length} approaches and landings")
//
//
//    val approachesByThreshold = approachesAndLandings.groupBy(_.threshold)
//
////    approachesByThreshold.foreach { case (threshold, approaches) =>
////      val crossingPoints = approaches.map(_.crossingPointInterpolated.altitudeMeters).sorted
////      println(s"val ${threshold.airport.icaoID}${threshold.name} = Array(${crossingPoints.mkString(", ")})")
////    }
//
//    val intervalLengthInMeters = 100
//
//    val interpolatedApproachesByThreshold = approachesByThreshold.map { case (threshold, approachesAndLandingsOneThreshold) =>
//
//      val interpolatedApproachesOneThreshold = approachesAndLandingsOneThreshold.map { approachAndLanding =>
//        ExtractionAndEstimation.interpolate(approachAndLanding.approach, intervalLengthInMeters)
//      }
//
//      (threshold, interpolatedApproachesOneThreshold)
//    }
//
//    // DAN YOU LEFT OFF HERE... "single-threaded" ApproachModel to be supplanted by "multi-threaded" TrajectoryTree.
//    // Will likely end up splitting ExtractionAndEstimation.meanTrajectory. Where it gets positionsAtThisDistance and
//    // then calculates a single AngleAndAltitudeWithStats, we'll send the data for clustering and then calculate multiple
//    // AngleAndAltitudeWithStats. Those will be the "trajectory segments" of our tree.
//
//    val approachModelsByThreshold = interpolatedApproachesByThreshold.map { case (threshold, interpolatedApproach) =>
//      val meanApproach = ExtractionAndEstimation.meanTrajectory(interpolatedApproach)
//      val approachModel = ApproachModel.newOption(threshold.center, meanApproach, threshold.geographicCalculator).get  // TODO It seems a little clunky how we're reaching into the threshold object here
//      (threshold, approachModel)
//    }
//
//    val thresholdsByApproachModel = approachModelsByThreshold.toSeq.map { x => (x._2, x._1)}.toMap
//
//    val approachModels = ApproachModels(approachModelsByThreshold.values)
//
//    // TEST SOME DATA AGAINST THE MODELS
//
//    println("ABOUT TO TEST SOME DATA")
//
//    val testDataGlob = "all--2022-12-0*.json"
//
//    val testDataFiles = IO.resolveGlob(dirPath, testDataGlob)
//    val testDataTrajectories = filesToTrajectories(testDataFiles)
//
//    val trajectoriesWithApproaches = testDataTrajectories.map { case (aircraftProfile, trajectory) =>
//
//      val positions = trajectory.positions
//
//      val trajectoryWithApproachSegments = TimeBasedPositionWithApproachSegment(positions.head, None) +: positions.sliding(2).toSeq.map { positionPair =>
//
//        val firstPosition = positionPair(0)
//        val secondPosition = positionPair(1)
//
//        val bestFitOption = approachModels.bestFit(firstPosition, secondPosition)
//
//        val approachSegmentWithDeviationOption = bestFitOption.filter(_.deviation.normalizedEuclideanDistance < 5.0).map { bestFit =>
//
//          val threshold = thresholdsByApproachModel(bestFit.model)
//          val thresholdDistanceMeters = threshold.distanceInMeters(secondPosition)
//          val verticalDevMeters = bestFit.deviation.altitudeDevMeters
//          val horizontalDevMeters = MathUtils.isoscelesBaseLength(bestFit.deviation.angleDevDegrees, bestFit.appliedDistributionInMeters.toDouble)
//          val normalizedEuclideanDistance = bestFit.deviation.normalizedEuclideanDistance
//
//          ApproachSegmentWithDeviation(threshold, thresholdDistanceMeters, verticalDevMeters, horizontalDevMeters, normalizedEuclideanDistance)
//        }
//
//        TimeBasedPositionWithApproachSegment(secondPosition, approachSegmentWithDeviationOption)
//      }
//
//      (aircraftProfile, trajectoryWithApproachSegments)
//    }
//
//    val json = Json.toJson(trajectoriesWithApproaches)(IO.trajectoriesWithApproachesWrites)
//    Files.write(Paths.get("/tmp/trajectoriesWithApproaches.json"), json.toString().getBytes())  // TODO Should specify encoding



//    val stringBuilder = new StringBuilder()
//
//    trajectoriesWithApproaches.foreach { trajectoryWithApproachSegments =>
//
//      stringBuilder.append("\n*** START TRAJECTORY ***\n")
//
//      trajectoryWithApproachSegments.foreach { case (position, approachSegment) =>
//
//        val message = approachSegment.map { deviation =>
//          s"${deviation.threshold.airport.icaoID}, ${deviation.threshold.name}: At ${deviation.thresholdDistanceMeters} m out, concern factor ${deviation.normalizedEuclideanDistance} (${deviation.verticalDevMeters} m too high/low, ${deviation.horizontalDevMeters} m too left/right)"
//        }.getOrElse("-")
//
//        stringBuilder.append(message).append("\n")
//
//      }
//      stringBuilder.append("*** END TRAJECTORY ***\n")
//    }
//
//    val outputFile = Paths.get("/tmp/output-deviations-etc.txt")
//    Files.write(outputFile, stringBuilder.toString().getBytes(StandardCharsets.UTF_8))

    // DAN, THE TASK NOW IS TO SWITCH THE ABOVE INFO FROM GETTING DUMPED INTO A FILE TO BEING PACKAGED AS JSON;
    // IT'S ADDING SIX MORE FIELDS PER fields-for-ui.ods; PROBABLY IN VEIN OF a7a2c00c2bc5f6b1531fbcb6842f2446690e3467

//        println(s"\n$aircraftProfile: ${strings.mkString(", ")}")
////        println(Json.toJson(trajectory)(IO.multipleTimeBasedPositionWrites))
//      }
//    }

    //
//    val sfo28L = Airports.sfo.thresholdByName("28L").get
//
//    val sfo28LApproaches = approachesByThreshold(sfo28L)
//    val altitudeOfCrossingPointsIn28LApproach = sfo28LApproaches.map(_.crossingPointInterpolated.altitudeMeters).sorted.mkString(", ")
//    println(s"*** Altitude of Threshold Crossing Point in 28L Approach ***\n$altitudeOfCrossingPointsIn28LApproach")
//    println(s"MEAN OF FOREGOING: ${sfo28LApproaches.map(_.crossingPointInterpolated.altitudeMeters).sum/sfo28LApproaches.length}")
//
//    val sfo28LInterpolatedApproaches = interpolatedApproachesByThreshold(sfo28L)
//
////    val heightAt100mSFO28LInterpolatedApproaches = sfo28LInterpolatedApproaches.flatMap { approach =>
////      approach.get(100).map(_.altitudeMeters)
////    }.sorted.mkString(", ")
////    println(s"*** 28L Approaches, interpolated height at 100 m ***\n$heightAt100mSFO28LInterpolatedApproaches")
//
//    val sfo28LMeanApproach = ExtractionAndEstimation.meanTrajectory(sfo28LInterpolatedApproaches).toSeq.sortBy(_._1)
//    println(s"*** 28L Mean Approach ***\n$sfo28LMeanApproach")
//
////    Seq(1000, 2000, 3000, 4000, 5000, 7500, 10000).foreach { distance =>
////      val angle = sfo28LInterpolatedApproaches.flatMap { approach => approach.get(distance).map(_.angleCompassDegrees) }.sorted
////      println(s"val approachesSorted${distance}m = Array( ${angle.mkString(", ")} )")
////    }
//
////    val interpolatedApproachesSmooshedTogether = interpolatedApproachesByThreshold.flatMap { case (_, s) => s.map(_.seqNoOffset) }
////
////    val stringBuilder = new StringBuilder()
////    stringBuilder.append("runway,icao24,WKT\n")
////
////    interpolatedApproachesSmooshedTogether.foreach { a =>
////      stringBuilder.append(s"\"A PLANE\",\"${IO.toWKT(a)}\"\n")
////    }
//
//
//
//    //    val approachesAndLandingsSorted = approachesAndLandings.sortBy(a => (a.threshold.airport.toString, a.threshold.name))  // TODO Change first param when we have actual airport name
////
////    val stringBuilder = new StringBuilder()
////    stringBuilder.append("runway,icao24,WKT\n")
////
////    approachesAndLandingsSorted.foreach { a =>
////      val icao24 = a.aircraftProfile.icao24
////      stringBuilder.append(s"\"${a.threshold.name}\",\"$icao24\",\"${IO.toWKT(a.approach :++ a.landing)}\"\n")
////    }
//
//
//
//
//
////    val outputFile = Paths.get("/tmp/output-interpolated.txt")
////    Files.write(outputFile, stringBuilder.toString().getBytes(StandardCharsets.UTF_8))

    println("Done!")
  }
}
