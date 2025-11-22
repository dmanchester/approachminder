package com.dmanchester.approachminder

import com.dmanchester.approachminder.Output.openSkyTrajectoriesToJson
import com.dmanchester.approachminder.SharedResources.sfoRunway28LApproachModel
import com.dmanchester.approachminder.typeswithbehavior.{DeviationFromMean, Trajectory}
import com.dmanchester.approachminder.typeswithoutbehavior.*
import org.specs2.mutable.*

class OutputSpec extends Specification {

  "openSkyTrajectoriesToJson" should {

    val minimalPosition = (
      OpenSkyPositionReport(
        1, // timePosition
        0, // lastContact; not represented in JSON
        2, // longitude
        3, // latitude
        None, // baroAltitude; not represented in JSON
        false, // onGround
        None, // velocity
        None, // trueTrack
        None, // verticalRate
        4, // altitudeMeters
        None, // squawk
        false, // spi; not represented in JSON
        ADSB // positionSource; not represented in JSON
      ),
      None
    )

    def stripNewlinesAndLeadingWhitespace(value: String): String = value.replaceAll("\n\\s*", "")

    "handle minimally and maximally specified positions (first and second positions, respectively)" in {

      val maximalPosition = (
        OpenSkyPositionReport(
          946641599, // timePosition
          0, // lastContact; not represented in JSON
          -122.079, // longitude; not scaled in JSON
          37.61, // latitude; not scaled in JSON
          None, // baroAltitude; not represented in JSON
          true, // onGround
          Some(125.6), // velocity; scaled to 0 decimal places in JSON (this value will round up)
          Some(6.4), // trueTrack; scaled to 0 decimal places in JSON (this value will round down)
          Some(7.89), // verticalRate; scaled to 1 decimal place in JSON (this value will round up)
          456.5, // altitudeMeters; scaled to 0 decimal places in JSON (this value will round down)
          Some("7700"), // squawk
          true, // spi; not represented in JSON
          MLAT // positionSource; not represented in JSON
        ),
        Some(
          ModelFitWithDisplayFields(
            ModelFit(
              sfoRunway28LApproachModel, // model; airport and runway represented in JSON
              DeviationFromMean(
                12.3, // angleDevInDegrees; not directly represented in JSON
                1.1, // angleStdDevs; used with altitudeStdDevs to calculate stdDevs for JSON, which is scaled to 1 decimal place (1.5486...; will round down)
                54.2, // altitudeDevInMeters; scaled to 0 decimal places in JSON (this value will round down)
                1.09 // altitudeStdDevs; see above note regarding angleStdDevs
              ),
              876.5 // distanceTestedAtInMeters; not directly represented in JSON
            ),
            43.2, // thresholdDistanceInMeters; scaled to 0 decimal places in JSON (this value will round down)
            10.9 // horizontalDevInMeters; scaled to 0 decimal places in JSON (this value will round up)
          )
        )
      )

      val trajectory = Trajectory.newOption(Seq(minimalPosition, maximalPosition), "abc123", Some("N1234"), Some(Large)).get
      val trajectoryJson = openSkyTrajectoriesToJson(Seq(trajectory))

      trajectoryJson mustEqual stripNewlinesAndLeadingWhitespace("""
        [
          {
            "icao24":"abc123","callsign":"N1234","category":"Large$","positions":{
              "1970-01-01T00:00:01Z":{
                "longitude":2,
                "latitude":3,
                "altitude":4,
                "onGround":false,
                "velocity":null,
                "trueTrack":null,
                "verticalRate":null,
                "squawk":null,
                "modelFit":null
              },
              "1999-12-31T11:59:59Z":{
                "longitude":-122.079,
                "latitude":37.61,
                "altitude":456,
                "onGround":true,
                "velocity":126,
                "trueTrack":6,
                "verticalRate":7.9,
                "squawk":"7700",
                "modelFit":{
                  "airport":"KSFO",
                  "runway":"28L",
                  "thresholdDistance":43,
                  "verticalDevMeters":54,
                  "horizontalDevMeters":11,
                  "stdDevs":1.5
                }
              }
            }
          }
        ]""")

      "handle a None callsign and category" in {

        val trajectory = Trajectory.newOption(Seq(minimalPosition, minimalPosition), "abc123", None, None).get
        val trajectoryJson = openSkyTrajectoriesToJson(Seq(trajectory))

        trajectoryJson must contain("\"callsign\":null,\"category\":null")
      }
    }
  }
}