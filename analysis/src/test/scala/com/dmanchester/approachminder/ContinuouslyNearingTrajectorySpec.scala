package com.dmanchester.approachminder

import SharedResources._

import org.specs2.mutable._

class ContinuouslyNearingTrajectorySpec extends Specification {

  "clip (object method)" should {

    // The trajectories of this method's tests rely on the points immediately below. They are
    // positioned as follows; "X" is the reference point:
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

    val referencePoint = LongLat(-118.67, 34.67)

    val pointA = LongLatAlt(-120, 38, 0)
    val pointB = LongLatAlt(-122, 38, 0)
    val pointC = LongLatAlt(-121, 37, 0)
    val pointD = LongLatAlt(-120, 36, 0)
    val pointE = LongLatAlt(-119, 35, 0)
    val pointF = LongLatAlt(-119, 36, 0)

    "start the continuously nearing trajectory at the first point of the input trajectory (provided distance criteria are met)" in {
      val trajectory = Seq(pointB, pointC, pointD, pointE)
      ContinuouslyNearingTrajectory.clip(trajectory, referencePoint, sfoCalculator).positions must beEqualTo(Seq(pointB, pointC, pointD, pointE))
    }

    "start the continuously nearing trajectory at the point the input trajectory becomes continuously nearing" in {
      val trajectory = Seq(pointA, pointB, pointC, pointD, pointE)
      ContinuouslyNearingTrajectory.clip(trajectory, referencePoint, sfoCalculator).positions must beEqualTo(Seq(pointB, pointC, pointD, pointE))
    }

    "produce a single-point continuously nearing trajectory" in {
      val trajectory = Seq(pointB, pointC, pointD, pointE, pointF)
      ContinuouslyNearingTrajectory.clip(trajectory, referencePoint, sfoCalculator).positions must beEqualTo(Seq(pointF))
    }

    "handle single-point input" in {
      val trajectory = Seq(pointB)
      ContinuouslyNearingTrajectory.clip(trajectory, referencePoint, sfoCalculator).positions must beEqualTo(Seq(pointB))
    }

    "handle no-point input" in {
      val trajectory = Seq.empty[TimeBasedPosition]
      ContinuouslyNearingTrajectory.clip(trajectory, referencePoint, sfoCalculator).positions must beEqualTo(Seq.empty[TimeBasedPosition])
    }
  }
}