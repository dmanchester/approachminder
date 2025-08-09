package com.dmanchester.approachminder.utils

import com.dmanchester.approachminder.SharedResources.*
import com.dmanchester.approachminder.typeswithbehavior.ContinuouslyNearingTrajectory
import com.dmanchester.approachminder.typeswithoutbehavior.{LongLat, LongLatAlt}
import com.dmanchester.approachminder.utils.TrajectoryUtils.{continuouslyNearingSegmentsEndingAt, continuouslyNearingSegmentsStartingAt}
import org.specs2.mutable.*

class TrajectoryUtilsSpec extends Specification {

  // Tests in this file rely on the seven-point trajectory from point A to point G and reference points X and Y, with
  // the points positioned as follows:
  //
  //             X
  //
  //             G
  //            /
  //           F
  //          /
  // A       E
  //  \     /
  //   B   D
  //    \ /
  //     C
  //
  //     Y

  private val pointA = LongLat(-122, 38)
  private val pointB = LongLat(-121, 37)
  private val pointC = LongLat(-120, 36)
  private val pointD = LongLat(-119, 37)
  private val pointE = LongLat(-118, 38)
  private val pointF = LongLat(-117, 39)
  private val pointG = LongLat(-116, 40)

  private val segmentIndexAB = 0
  private val segmentIndexBC = 1
  private val segmentIndexCD = 2
  private val segmentIndexDE = 3
  private val segmentIndexEF = 4

  private val pointX = LongLat(-116, 41)
  private val pointY = LongLat(-120, 35)

  private val trajectory = trajectoryFromPositions(Seq(pointA, pointB, pointC, pointD, pointE, pointF, pointG))

  "continuouslyNearingSegmentsStartingAt" should {

    "handle a starting segment that doesn't continuously near the reference point" in {
      continuouslyNearingSegmentsStartingAt(trajectory, segmentIndexCD, pointY, sfoCalculator) mustEqual 0
    }

    "handle a trajectory whose continuously nearing segments end before the trajectory does" in {
      continuouslyNearingSegmentsStartingAt(trajectory, segmentIndexAB, pointY, sfoCalculator) mustEqual 2
    }

    "handle a trajectory whose continuously nearing segments end when the trajectory does" in {
      continuouslyNearingSegmentsStartingAt(trajectory, segmentIndexDE, pointX, sfoCalculator) mustEqual 3
    }

    "throw on segmentIndex too low" in {
      continuouslyNearingSegmentsStartingAt(trajectory, -1, pointX, sfoCalculator) must throwA[IndexOutOfBoundsException]
    }

    "throw on segmentIndex too high" in {
      continuouslyNearingSegmentsStartingAt(trajectory, trajectory.segments.length, pointX, sfoCalculator) must throwA[IndexOutOfBoundsException]
    }
  }

  "continuouslyNearingSegmentsEndingAt" should {

    "handle an ending segment that doesn't continuously near the reference point" in {
      continuouslyNearingSegmentsEndingAt(trajectory, segmentIndexCD, pointY, sfoCalculator) mustEqual 0
    }

    "handle a trajectory whose continuously nearing segments start before the trajectory does" in {
      continuouslyNearingSegmentsEndingAt(trajectory, segmentIndexEF, pointX, sfoCalculator) mustEqual 3
    }

    "handle a trajectory whose continuously nearing segments start when the trajectory does" in {
      continuouslyNearingSegmentsEndingAt(trajectory, segmentIndexBC, pointY, sfoCalculator) mustEqual 2
    }

    "throw on segmentIndex too low" in {
      continuouslyNearingSegmentsEndingAt(trajectory, -1, pointX, sfoCalculator) must throwA[IndexOutOfBoundsException]
    }

    "throw on segmentIndex too high" in {
      continuouslyNearingSegmentsEndingAt(trajectory, trajectory.segments.length, pointX, sfoCalculator) must throwA[IndexOutOfBoundsException]
    }
  }

  "interpolateAtIntervals" should {

    "interpolate points on a trajectory whose segments cross differing numbers of rings: 0 rings (second segment), 1 ring (first and fourth segments), and more than 1 (third segment)" in {

      val referencePoint = LongLat(-122, 38)
      val pointR = LongLatAlt(-122, 40.7, 500) // 299.7 km; between 280 and 350 km
      val pointS = LongLatAlt(-121.9, 40.3, 400) // 255.4 km; between 210 and 280 km
      val pointT = LongLatAlt(-121.9, 40.1, 300) // 233.2 km; also between 210 and 280 km
      val pointU = LongLatAlt(-122.1, 39.2, 200) // 133.5 km; between 70 and 140 km (no points between 140 and 210 km)
      val pointV = LongLatAlt(-122.1, 38.5, 100) // 56.2 km; less than 70 km
      val sourcePositions = trajectoryFromPositions(Seq(pointR, pointS, pointT, pointU, pointV))

      val (sourceTrajectory, _) = ContinuouslyNearingTrajectory.newOption(sourcePositions, 0, referencePoint, sfoCalculator).get

      val targetTrajectory = TrajectoryUtils.interpolateAtIntervals(sourceTrajectory, 70000)
      val targetPositions = targetTrajectory.positions

      targetPositions.size mustEqual 4

      targetPositions(70000).angle.asCompassDegrees must beCloseTo(352.235400 within significantFigures) // ~(-122.100010, 38.625875)
      targetPositions(70000).altitudeMeters must beCloseTo(117.981271 within significantFigures)

      targetPositions(140000).angle.asCompassDegrees must beCloseTo(356.305703 within significantFigures) // ~(-122.086899, 39.259719)
      targetPositions(140000).altitudeMeters must beCloseTo(206.633631 within significantFigures)

      targetPositions(210000).angle.asCompassDegrees must beCloseTo(0.613227 within significantFigures) // ~(-121.946771, 39.891761)
      targetPositions(210000).altitudeMeters must beCloseTo(276.85694 within significantFigures)

      targetPositions(280000).angle.asCompassDegrees must beCloseTo(0.138516 within significantFigures) // ~(-121.955503, 40.522587)
      targetPositions(280000).altitudeMeters must beCloseTo(455.643761 within significantFigures)
    }
  }
}
