package com.dmanchester.approachminder

import play.api.libs.json.Json

import java.nio.file.{Files, Path, Paths}
import java.nio.charset.StandardCharsets

object ThrowawayMain {

  def filesToTrajectories(files: Seq[Path]): Seq[(AircraftProfile, Seq[TimeBasedPosition])] = {

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

    val approachesAndLandings = trajectories.flatMap { case (aircraftProfile, trajectory) =>
      ExtractionAndEstimation.approachesAndLandings(aircraftProfile, trajectory, thresholds)
    }

    println(s"${approachesAndLandings.length} approaches and landings")


    val approachesByThreshold = approachesAndLandings.groupBy(_.threshold)

//    approachesByThreshold.foreach { case (threshold, approaches) =>
//      val crossingPoints = approaches.map(_.crossingPointInterpolated.altitudeMeters).sorted
//      println(s"val ${threshold.airport.icaoID}${threshold.name} = Array(${crossingPoints.mkString(", ")})")
//    }

    val intervalLengthInMeters = 100

    val interpolatedApproachesByThreshold = approachesByThreshold.map { case (threshold, approachesAndLandingsOneThreshold) =>

      val interpolatedApproachesOneThreshold = approachesAndLandingsOneThreshold.map { approachAndLanding =>
        ExtractionAndEstimation.interpolate(approachAndLanding.approach, intervalLengthInMeters)
      }

      (threshold, interpolatedApproachesOneThreshold)
    }

    val approachModelsByThreshold = interpolatedApproachesByThreshold.map { case (threshold, interpolatedApproach) =>
      val meanApproach = ExtractionAndEstimation.meanTrajectory(interpolatedApproach)
      val approachModel = ApproachModel.newOption(threshold.center, meanApproach, threshold.geographicCalculator).get  // TODO It seems a little clunky how we're reaching into the threshold object here
      (threshold, approachModel)
    }

    val thresholdsByApproachModel = approachModelsByThreshold.toSeq.map { x => (x._2, x._1)}.toMap

    val approachModels = ApproachModels(approachModelsByThreshold.values)

    // TEST SOME DATA AGAINST THE MODELS

    println("ABOUT TO TEST SOME DATA")

    val testDataGlob = "all--2022-12-0*.json"

    val testDataFiles = IO.resolveGlob(dirPath, testDataGlob)
    val testDataTrajectories = filesToTrajectories(testDataFiles)

    testDataTrajectories.foreach { case (aircraftProfile, trajectory) =>

      if (trajectory.size >= 2) {

        val slidingWindowOverTrajectory = trajectory.sliding(2).toSeq

        val strings = slidingWindowOverTrajectory.map { pointPair =>

          val fitOption = approachModels.bestFit(pointPair(0), pointPair(1))

          fitOption.filter(_._3.normalizedEuclideanDistance < 5.0).map { case (model, appliedDistributionInMeters, deviation) =>
            val threshold = thresholdsByApproachModel(model)
            s"(${threshold.airport.icaoID}${threshold.name},${appliedDistributionInMeters.toInt},$deviation)"
          }.getOrElse("-")
        }

        println(s"\n$aircraftProfile: ${strings.mkString(", ")}")
//        println(Json.toJson(trajectory)(IO.multipleTimeBasedPositionWrites))
      }
    }

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
