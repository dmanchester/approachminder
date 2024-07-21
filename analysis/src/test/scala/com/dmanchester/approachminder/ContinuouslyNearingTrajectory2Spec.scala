package com.dmanchester.approachminder

import com.dmanchester.approachminder.SharedResources._
import org.specs2.mutable._

class ContinuouslyNearingTrajectory2Spec extends Specification {

  // The trajectories of this class's tests rely on the points immediately below. They are positioned as follows; "X" is
  // the reference point:
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

  "fromStartOfTrajectoryOption" should {

    "handle a trajectory that continuously nears the reference point over the trajectory's entirety" in {
      val trajectory = Trajectory.newOption(Seq(pointB, pointC, pointD, pointE)).get
      ContinuouslyNearingTrajectory2.fromStartOfTrajectoryOption(trajectory, referencePoint, sfoCalculator).get.positions must beEqualTo(Seq(pointB, pointC, pointD, pointE))
    }

    "handle a trajectory that continuously nears the reference point before deviating away from it" in {
      val trajectory = Trajectory.newOption(Seq(pointB, pointC, pointD, pointE, pointF)).get
      ContinuouslyNearingTrajectory2.fromStartOfTrajectoryOption(trajectory, referencePoint, sfoCalculator).get.positions must beEqualTo(Seq(pointB, pointC, pointD, pointE))
    }

    "handle a trajectory whose first segment doesn't continuously near the reference point" in {
      val trajectory = Trajectory.newOption(Seq(pointA, pointB, pointC)).get
      ContinuouslyNearingTrajectory2.fromStartOfTrajectoryOption(trajectory, referencePoint, sfoCalculator) must beNone
    }
  }

  "fromEndOfTrajectoryOption" should {

    "handle a trajectory that continuously nears the reference point over the trajectory's entirety" in {
      val trajectory = Trajectory.newOption(Seq(pointB, pointC, pointD, pointE)).get
      ContinuouslyNearingTrajectory2.fromEndOfTrajectoryOption(trajectory, referencePoint, sfoCalculator).get.positions must beEqualTo(Seq(pointB, pointC, pointD, pointE))
    }

    "handle a trajectory that continuously nears the reference point after initially deviating away from it" in {
      val trajectory = Trajectory.newOption(Seq(pointA, pointB, pointC, pointD, pointE)).get
      ContinuouslyNearingTrajectory2.fromEndOfTrajectoryOption(trajectory, referencePoint, sfoCalculator).get.positions must beEqualTo(Seq(pointB, pointC, pointD, pointE))
    }

    "handle a trajectory whose last segment doesn't continuously near the reference point" in {
      val trajectory = Trajectory.newOption(Seq(pointD, pointE, pointF)).get
      ContinuouslyNearingTrajectory2.fromEndOfTrajectoryOption(trajectory, referencePoint, sfoCalculator) must beNone
    }
  }

  "fromMiddleOfTrajectoryOption" should {

    val fullTrajectory = Trajectory.newOption(Seq(pointA, pointB, pointC, pointD, pointE, pointF)).get

    "handle a trajectory where the specified middle segment is at the start of a continuously nearing portion" in {
      val (continuouslyNearingTrajectory, segmentsAfterMiddleIncluded) = ContinuouslyNearingTrajectory2.fromMiddleOfTrajectoryOption(fullTrajectory, 1, referencePoint, sfoCalculator).get
      continuouslyNearingTrajectory.positions must beEqualTo(Seq(pointB, pointC, pointD, pointE))
      segmentsAfterMiddleIncluded must beEqualTo(2)
    }

    "handle a trajectory where the specified middle segment is in the middle of a continuously nearing portion" in {
      val (continuouslyNearingTrajectory, segmentsAfterMiddleIncluded) = ContinuouslyNearingTrajectory2.fromMiddleOfTrajectoryOption(fullTrajectory, 2, referencePoint, sfoCalculator).get
      continuouslyNearingTrajectory.positions must beEqualTo(Seq(pointB, pointC, pointD, pointE))
      segmentsAfterMiddleIncluded must beEqualTo(1)
    }

    "handle a trajectory where the specified middle segment is at the end of a continuously nearing portion" in {
      val (continuouslyNearingTrajectory, segmentsAfterMiddleIncluded) = ContinuouslyNearingTrajectory2.fromMiddleOfTrajectoryOption(fullTrajectory, 3, referencePoint, sfoCalculator).get
      continuouslyNearingTrajectory.positions must beEqualTo(Seq(pointB, pointC, pointD, pointE))
      segmentsAfterMiddleIncluded must beEqualTo(0)
    }

    "handle a trajectory that continuously nears the reference point before deviating away from it" in {
      val trajectory = Trajectory.newOption(Seq(pointB, pointC, pointD, pointE, pointF)).get
      val (continuouslyNearingTrajectory, segmentsAfterMiddleIncluded) = ContinuouslyNearingTrajectory2.fromMiddleOfTrajectoryOption(trajectory, 0, referencePoint, sfoCalculator).get
      continuouslyNearingTrajectory.positions must beEqualTo(Seq(pointB, pointC, pointD, pointE))
      segmentsAfterMiddleIncluded must beEqualTo(2)
    }

    "handle a trajectory where the specified middle segment doesn't continuously near the reference point" in {
      val trajectory = Trajectory.newOption(Seq(pointA, pointB, pointC)).get
      ContinuouslyNearingTrajectory2.fromStartOfTrajectoryOption(trajectory, referencePoint, sfoCalculator) must beNone
    }

    "throw on middleSegmentIndex too low" in {
      ContinuouslyNearingTrajectory2.fromMiddleOfTrajectoryOption(fullTrajectory, -1, referencePoint, sfoCalculator) must throwA[IndexOutOfBoundsException]
    }

    "throw on middleSegmentIndex too high" in {
      ContinuouslyNearingTrajectory2.fromMiddleOfTrajectoryOption(fullTrajectory, 5, referencePoint, sfoCalculator) must throwA[IndexOutOfBoundsException]
    }
  }

}