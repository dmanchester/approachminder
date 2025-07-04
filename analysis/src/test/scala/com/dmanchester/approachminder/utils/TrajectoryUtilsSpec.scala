package com.dmanchester.approachminder.utils

import com.dmanchester.approachminder.LongLat
import com.dmanchester.approachminder.SharedResources.*
import com.dmanchester.approachminder.typeswithbehavior.Trajectory
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
  private val segmentIndexFG = 5

  private val pointX = LongLat(-116, 41)
  private val pointY = LongLat(-120, 35)

  private val trajectory = Trajectory.newOption(Seq(pointA, pointB, pointC, pointD, pointE, pointF, pointG), "icao24", None, None).get

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
  }
}
