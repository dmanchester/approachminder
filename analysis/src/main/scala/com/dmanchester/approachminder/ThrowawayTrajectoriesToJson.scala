package com.dmanchester.approachminder

import com.dmanchester.approachminder.ThrowawayMain.filesToTrajectories
import play.api.libs.json.Json

import java.nio.file.{Files, Paths}

object ThrowawayTrajectoriesToJson {

  def main(args: Array[String]): Unit = {

    val dirPath = Paths.get("/home/dan/flight-tracking/opensky-data-as-of--2013-01-12--0029/")
    val testDataGlob = "all--2022-11-20*.json"

    val testDataFiles = IO.resolveGlob(dirPath, testDataGlob)
    val testDataTrajectories = filesToTrajectories(testDataFiles)

//    val json = Json.toJson(testDataTrajectories)(IO.trajectoriesWithApproachesWrites)
//    Files.write(Paths.get("/tmp/ThrowawayTrajectoriesToJson.json"), json.toString().getBytes())  // TODO Should specify encoding
  }
}
