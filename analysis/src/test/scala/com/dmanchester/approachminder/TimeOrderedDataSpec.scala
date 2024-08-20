package com.dmanchester.approachminder

import org.specs2.mutable._

class TimeOrderedDataSpec extends Specification {

  val time10 = TimeWithSomeText(10)
  val time20 = TimeWithSomeText(20)
  val time21 = TimeWithSomeText(21)
  val time15Winner = TimeWithSomeText(15, "winner")
  val time15Loser1 = TimeWithSomeText(15, "loser 1")
  val time15Loser2 = TimeWithSomeText(15, "loser 2")

  "create" should {

    "time-order the data" in {
      val times = IndexedSeq(time20, time10, time21)
      val timeOrderedData = TimeOrderedData.create(times)
      timeOrderedData.seq mustEqual IndexedSeq(time10, time20, time21)
    }

    "remove time-conflicting elements, picking the positionally last element with a given time as the winner and removing any other elements with that time" in {
      val times = IndexedSeq(time21, time15Loser1, time10, time15Loser2, time15Winner, time20)
      val timeOrderedData = TimeOrderedData.create(times)
      timeOrderedData.seq mustEqual IndexedSeq(time10, time15Winner, time20, time21)
    }
  }

//  "segmentIntoTrajectoriesByTime" should {
//
//    val minTimeInSeconds = 10
//
//    "handle multiple- and single-point trajectories" in {
//      val historicalPositions = Seq(time10, time20, time21, time40, time42)
//      val expectedTrajectories = Seq(Trajectory.newOption(Seq(time20, time21)), Trajectory.newOption(Seq(time40, time42))).flatten
//      GroupingSortingFiltering.segmentIntoTrajectoriesByTime(historicalPositions, minTimeInSeconds) must beEqualTo(expectedTrajectories)
//    }
//
//    "handle a single-point position history" in {
//      val historicalPositions = Seq(time10)
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