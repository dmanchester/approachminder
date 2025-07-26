package com.dmanchester.approachminder.utils

import com.dmanchester.approachminder.Airports.{oak, sfo}
import com.dmanchester.approachminder.SharedResources.{beCloseInThreeDimensionsTo, significantFigures, trajectoryFromPositions}
import com.dmanchester.approachminder.typeswithoutbehavior.{LongLatAlt, RunwayAndReferencePoint}
import org.specs2.mutable.Specification

class ApproachesAndLandingsExtractionSpec extends Specification {

  "extract" should {

    // Points K - L are laid out as follows; the runway at lower-left is SFO's 10L/28R; the one at upper-right is OAK's 12/30:
    //
    //                 P
    //                / \
    //               /   /\
    //              /    \ \
    //             /      \Q\
    //            O        \ \
    // L---K     /          \/
    //  \       /
    //   /\    /
    //   \M\  /
    //    \ \
    //     \N\
    //      \/

    val pointK = LongLatAlt(-122.3953887, 37.6309182, 40.0)
    val pointL = LongLatAlt(-122.3978134, 37.6307143, 30.0)
    val pointM = LongLatAlt(-122.3913976, 37.6279783, 20.0)
    val pointN = LongLatAlt(-122.3577305, 37.6138547, 10.0)
    val pointO = LongLatAlt(-122.3069331, 37.6825065, 100.0)
    val pointP = LongLatAlt(-122.2443625, 37.7216562, 50.0)
    val pointQ = LongLatAlt(-122.2303292, 37.7121848, 15.0)

    val runwaysAndReferencePoints = (sfo.runways :++ oak.runways).map { runway =>
      RunwayAndReferencePoint(runway, runway.opposite.thresholdCenter)
    }

    "extract instances of ApproachAndLanding from a trajectory, correctly allocating positions to each ApproachAndLanding, associating runways, and interpolating crossing points" in {

      val trajectory = trajectoryFromPositions(Seq(pointK, pointL, pointM, pointN, pointO, pointP, pointQ))

      val approachesAndLandings = ApproachesAndLandingsExtraction.extract(trajectory, runwaysAndReferencePoints)

      approachesAndLandings.length must beEqualTo(2)

      approachesAndLandings(0).trajectory.positions must beEqualTo(Seq(pointL, pointM, pointN))
      approachesAndLandings(0).runway must beEqualTo(sfo.getRunwayByName("10L"))
      approachesAndLandings(0).crossingPointInterpolated must beCloseInThreeDimensionsTo(LongLatAlt(-122.393345, 37.628809, 23.035889), significantFigures) // confirmed correctness visually

      approachesAndLandings(1).trajectory.positions must beEqualTo(Seq(pointO, pointP, pointQ))
      approachesAndLandings(1).runway must beEqualTo(oak.getRunwayByName("12"))
      approachesAndLandings(1).crossingPointInterpolated must beCloseInThreeDimensionsTo(LongLatAlt(-122.242067, 37.720108, 44.276624), significantFigures) // confirmed correctness visually
    }
  }
}
