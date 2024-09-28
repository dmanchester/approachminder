package com.dmanchester.approachminder

import org.specs2.mutable._

class GroupingSortingFilteringSpec extends Specification {

//  val timeA = Time(10)
//  val timeB = Time(20)
//  val timeC = Time(21)
//  val timeD = Time(40)
//  val timeDRepeat1 = Time(40)
//  val timeDRepeat2 = Time(40)
//  val timeE = Time(42)
//

//
//  "cleanPositionsWithSameTime" should {
//
//    "given positions with the same time, pick the position furthest down in the input as the winner and discard the others of that time." in {
//
//      val positions = Seq(timeA, timeB, timeC, timeD, timeDRepeat1, timeDRepeat2, timeE)
//      val expectedCleanedPositions = Seq(timeA, timeB, timeC, timeDRepeat2, timeE)
//      GroupingSortingFiltering.cleanPositionsWithSameTime(positions) must beEqualTo(expectedCleanedPositions)
//    }
//  }
//
//  "segmentIntoTrajectoriesByTime" should {
//
//    val minTimeInSeconds = 10
//
//    "handle multiple- and single-point trajectories" in {
//      val historicalPositions = Seq(timeA, timeB, timeC, timeD, timeE)
//      val expectedTrajectories = Seq(Trajectory.newOption(Seq(timeB, timeC)), Trajectory.newOption(Seq(timeD, timeE))).flatten
//      GroupingSortingFiltering.segmentIntoTrajectoriesByTime(historicalPositions, minTimeInSeconds) must beEqualTo(expectedTrajectories)
//    }
//
//    "handle a single-point position history" in {
//      val historicalPositions = Seq(timeA)
//      val expectedTrajectories = Seq.empty[Trajectory[Time]]
//      GroupingSortingFiltering.segmentIntoTrajectoriesByTime(historicalPositions, minTimeInSeconds) must beEqualTo(expectedTrajectories)
//    }
//
//    "handle a no-point position history" in {
//      val historicalPositions = Seq.empty[TimeBasedPosition]
//      val expectedTrajectories = Seq.empty[TimeBasedPosition]
//      GroupingSortingFiltering.segmentIntoTrajectoriesByTime(historicalPositions, minTimeInSeconds) must beEqualTo(expectedTrajectories)
//    }
//  }
}