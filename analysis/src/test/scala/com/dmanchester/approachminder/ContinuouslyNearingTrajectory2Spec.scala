package com.dmanchester.approachminder

import com.dmanchester.approachminder.SharedResources._
import org.specs2.mutable._

class ContinuouslyNearingTrajectory2Spec extends Specification {

  // The position sequences of this class's tests rely on the points immediately below. They are positioned as follows;
  // "X" is the reference point:
  //
  //  B-----A
  //   \
  //    \
  //     C
  //      \
  //       \
  //        D   F
  //         \  |
  //          \ |
  //           E
  //
  //            X
  //
  // Of note:
  //
  //   * The sequence B-C-D-E continuously nears X.
  //   * Point A is closer to X than B.
  //   * Point F is farther from X than E.

  private val referencePoint = LongLat(-118.67, 34.67)

  private val pointA = LongLatAlt(-120, 38, 0)
  private val pointB = LongLatAlt(-122, 38, 0)
  private val pointC = LongLatAlt(-121, 37, 0)
  private val pointD = LongLatAlt(-120, 36, 0)
  private val pointE = LongLatAlt(-119, 35, 0)
  private val pointF = LongLatAlt(-119, 36, 0)

  private val positionsABCDEF = Seq(pointA, pointB, pointC, pointD, pointE, pointF)
  private val positionsBCDE = Seq(pointB, pointC, pointD, pointE)

  "newOption" should {

    "handle a positions sequence where the specified segment is at the start of a continuously nearing portion" in {
      val (continuouslyNearingTrajectory, segmentsAfterMiddleIncluded) = ContinuouslyNearingTrajectory2.newOption(positionsABCDEF, 1, referencePoint, sfoCalculator).get
      continuouslyNearingTrajectory.positions must beEqualTo(positionsBCDE)
      segmentsAfterMiddleIncluded must beEqualTo(2)
    }

    "handle a positions sequence where the specified segment is in the middle of a continuously nearing portion" in {
      val (continuouslyNearingTrajectory, segmentsAfterMiddleIncluded) = ContinuouslyNearingTrajectory2.newOption(positionsABCDEF, 2, referencePoint, sfoCalculator).get
      continuouslyNearingTrajectory.positions must beEqualTo(positionsBCDE)
      segmentsAfterMiddleIncluded must beEqualTo(1)
    }

    "handle a positions sequence where the specified segment is at the end of a continuously nearing portion" in {
      val (continuouslyNearingTrajectory, segmentsAfterMiddleIncluded) = ContinuouslyNearingTrajectory2.newOption(positionsABCDEF, 3, referencePoint, sfoCalculator).get
      continuouslyNearingTrajectory.positions must beEqualTo(positionsBCDE)
      segmentsAfterMiddleIncluded must beEqualTo(0)
    }

    "handle a positions sequence that continuously nears the reference point from its start before deviating away from the reference point" in {
      val positions = Seq(pointB, pointC, pointD, pointE, pointF)
      val (continuouslyNearingTrajectory, segmentsAfterMiddleIncluded) = ContinuouslyNearingTrajectory2.newOption(positions, 0, referencePoint, sfoCalculator).get
      continuouslyNearingTrajectory.positions must beEqualTo(positionsBCDE)
      segmentsAfterMiddleIncluded must beEqualTo(2)
    }

    "handle a positions sequence where the specified segment doesn't continuously near the reference point" in {
      val positions = Seq(pointA, pointB, pointC)
      ContinuouslyNearingTrajectory2.newOption(positions, 0, referencePoint, sfoCalculator) must beNone
    }

    "throw on segmentIndex too low" in {
      ContinuouslyNearingTrajectory2.newOption(positionsABCDEF, -1, referencePoint, sfoCalculator) must throwA[IndexOutOfBoundsException]
    }

    "throw on segmentIndex too high" in {
      ContinuouslyNearingTrajectory2.newOption(positionsABCDEF, 5, referencePoint, sfoCalculator) must throwA[IndexOutOfBoundsException]
    }
  }
}