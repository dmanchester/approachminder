package com.dmanchester.approachminder

import org.specs2.mutable._
import SharedResources._

import com.dmanchester.approachminder.Airports.oak
import com.dmanchester.approachminder.Airports.sfo

class ExtractionAndEstimationSpec extends Specification {

  "subtrajectoryWithinPolygon" should {

    "get the points before the first one outside the polygon" in {
      val trajectory = Seq(sfoPointB, sfoPointE, sfoPointD)
      ExtractionAndEstimation.subtrajectoryWithinPolygon(trajectory, sfoRunwaySurface28L10R, sfoCalculator) must beEqualTo(Seq(sfoPointB, sfoPointE))
    }

    "get all points when all are within the polygon" in {
      val trajectory = Seq(sfoPointB, sfoPointE)
      ExtractionAndEstimation.subtrajectoryWithinPolygon(trajectory, sfoRunwaySurface28L10R, sfoCalculator) must beEqualTo(Seq(sfoPointB, sfoPointE))
    }

    "handle no initial points being within the polygon, even if subsequent ones are within it" in {
      val trajectory = Seq(sfoPointD, sfoPointB)
      ExtractionAndEstimation.subtrajectoryWithinPolygon(trajectory, sfoRunwaySurface28L10R, sfoCalculator) must beEqualTo(Seq.empty[TimeBasedPosition])
    }

    "handle a single point within the polygon" in {
      val trajectory = Seq(sfoPointB)
      ExtractionAndEstimation.subtrajectoryWithinPolygon(trajectory, sfoRunwaySurface28L10R, sfoCalculator) must beEqualTo(Seq(sfoPointB))
    }

    "handle no-point input" in {
      val trajectory = Seq.empty[TimeBasedPosition]
      ExtractionAndEstimation.subtrajectoryWithinPolygon(trajectory, sfoRunwaySurface28L10R, sfoCalculator) must beEqualTo(Seq.empty[TimeBasedPosition])
    }
  }

  "approachesAndLandings" should {

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

    val thresholds = Thresholds(sfo.thresholds :++ oak.thresholds)

    val stubProfile = AircraftProfile("(icao24)", Some("(callsign)"), None)

    "determine a trajectory's approaches and landings, allocating the correct positions to each; correctly associate thresholds; and correctly interpolate crossing points" in {

      val trajectory = Trajectory.newOption(Seq(pointK, pointL, pointM, pointN, pointO, pointP, pointQ)).get

      val approachesAndLandings = ExtractionAndEstimation.approachesAndLandings(stubProfile, trajectory, thresholds)

      approachesAndLandings.length must beEqualTo(2)

      approachesAndLandings(0).threshold must beEqualTo(sfo.thresholdByName("10L").get)
      approachesAndLandings(0).approach.length must beEqualTo(1) // point L
      approachesAndLandings(0).crossingPointInterpolated must beCloseInThreeDimensionsTo(LongLatAlt(-122.393345, 37.628809, 23.035889), significantFigures) // confirmed correctness visually
      approachesAndLandings(0).landing.length must beEqualTo(2) // points M, N

      approachesAndLandings(1).threshold must beEqualTo(oak.thresholdByName("12").get)
      approachesAndLandings(1).approach.length must beEqualTo(2) // points O, P
      approachesAndLandings(1).crossingPointInterpolated must beCloseInThreeDimensionsTo(LongLatAlt(-122.242067, 37.720108, 44.276624), significantFigures) // confirmed correctness visually
      approachesAndLandings(1).landing.length must beEqualTo(1) // point Q
    }
  }

  "interpolate" should {

    val referencePoint = LongLat(-122, 38)
    val pointR = LongLatAlt(-122, 40.7, 500) // 299.7 km; between 280 and 350 km
    val pointS = LongLatAlt(-121.9, 40.3, 400) // 255.4 km; between 210 and 280 km
    val pointT = LongLatAlt(-121.9, 40.1, 300) // 233.2 km; also between 210 and 280 km
    val pointU = LongLatAlt(-122.1, 39.2, 200) // 133.5 km; between 70 and 140 km (no points between 140 and 210 km)
    val pointV = LongLatAlt(-122.1, 38.5, 100) // 56.2 km; less than 70 km

    val trajectoryFourPoints = Seq(pointR, pointS, pointT, pointU)
    val trajectoryFivePoints = trajectoryFourPoints :+ pointV

    "interpolate points on a trajectory whose segments cross differing numbers of rings: 0 rings (second segment), 1 ring (first and fourth segments), and more than 1 (third segment)" in {
      val trajectory = ContinuouslyNearingTrajectory.clip(trajectoryFivePoints, referencePoint, sfoCalculator)
      val interpolatedPoints = ExtractionAndEstimation.interpolate(trajectory, 70000)

      interpolatedPoints.size mustEqual 4

      interpolatedPoints(70000).angle.toCompassDegrees must beCloseTo(352.235400 within significantFigures) // ~(-122.100010, 38.625875)
      interpolatedPoints(70000).altitudeMeters must beCloseTo(117.981271 within significantFigures)

      interpolatedPoints(140000).angle.toCompassDegrees must beCloseTo(356.305703 within significantFigures) // ~(-122.086899, 39.259719)
      interpolatedPoints(140000).altitudeMeters must beCloseTo(206.633631 within significantFigures)

      interpolatedPoints(210000).angle.toCompassDegrees must beCloseTo(0.613227 within significantFigures) // ~(-121.946771, 39.891761)
      interpolatedPoints(210000).altitudeMeters must beCloseTo(276.85694 within significantFigures)

      interpolatedPoints(280000).angle.toCompassDegrees must beCloseTo(0.138516 within significantFigures) // ~(-121.955503, 40.522587)
      interpolatedPoints(280000).altitudeMeters must beCloseTo(455.643761 within significantFigures)

    }
  }

  "meanTrajectories" should {

    "calculate mean positions at all distances where at least two positions are available" in {

      // The three trajectories for this test are laid out as follows (format: angle, altitude),
      // giving rise to two mean positions (format: mean angle (angle variance), mean altitude
      // (altitude variance)):
      //
      // Distance    Traj. A     Traj. B     Traj. C            Mean Position        Positions
      // --------   ---------   ---------   ---------       ----------------------   ---------
      //   1.0      1.0, 10.0       X           X
      //   1.5      2.0, 12.0   3.0, 12.0       X      ==>  2.5 (0.5), 12.0 (0.0)        2
      //   2.0      3.0, 14.0   6.0, 16.0   9.0, 24.0       6.0 (9.0), 18.0 (28.0)       3
      //   2.5         X        9.0, 20.0       X

      val bd_1_0 = BigDecimal("1.0")  // "bd" = "BigDecimal"
      val bd_1_5 = BigDecimal("1.5")
      val bd_2_0 = BigDecimal("2.0")
      val bd_2_5 = BigDecimal("2.5")

      // FIXME Put in real altitude values; and check them at bottom

      val trajectories = Seq(
        Map(bd_1_0 -> AngleAndAltitude(PolarAngle.fromCompassDegrees(1.0), 10.0), bd_1_5 -> AngleAndAltitude(PolarAngle.fromCompassDegrees(2.0), 12.0), bd_2_0 -> AngleAndAltitude(PolarAngle.fromCompassDegrees(3.0), 14.0)),
        Map(bd_1_5 -> AngleAndAltitude(PolarAngle.fromCompassDegrees(3.0), 12.0), bd_2_0 -> AngleAndAltitude(PolarAngle.fromCompassDegrees(6.0), 16.0), bd_2_5 -> AngleAndAltitude(PolarAngle.fromCompassDegrees(9.0), 20.0)),
        Map(bd_2_0 -> AngleAndAltitude(PolarAngle.fromCompassDegrees(9.0), 24.0))
      )

      val meanTrajectory = ExtractionAndEstimation.meanTrajectory(trajectories)

      meanTrajectory.size mustEqual 2

      meanTrajectory(bd_1_5).angle.toCompassDegrees must beCloseTo(2.5 within significantFigures)
      meanTrajectory(bd_1_5).angleStdDevDegrees must beCloseTo(0.707107 within significantFigures)
      meanTrajectory(bd_1_5).altitudeMeters must beCloseTo(12.0 within significantFigures)
      meanTrajectory(bd_1_5).altitudeStdDevMeters must beCloseTo(0.0 within significantFigures)
      meanTrajectory(bd_1_5).sourceCount mustEqual 2

      meanTrajectory(bd_2_0).angle.toCompassDegrees must beCloseTo(6.0 within significantFigures)
      meanTrajectory(bd_2_0).angleStdDevDegrees must beCloseTo(3.0 within significantFigures)
      meanTrajectory(bd_2_0).altitudeMeters must beCloseTo(18.0 within significantFigures)
      meanTrajectory(bd_2_0).altitudeStdDevMeters must beCloseTo(5.291503 within significantFigures)
      meanTrajectory(bd_2_0).sourceCount mustEqual 3
    }
  }
}