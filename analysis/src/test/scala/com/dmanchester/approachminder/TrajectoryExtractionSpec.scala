package com.dmanchester.approachminder

import com.dmanchester.approachminder.TrajectoryExtraction.partitionByICAO24
import org.specs2.mutable.*

class TrajectoryExtractionSpec extends Specification {

  "partitionElementsByICAO24" should {

    "handle an empty Seq" in {
      val elements = Seq.empty[HasICAO24]
      val partitionedElements = partitionByICAO24(elements)
      partitionedElements must empty
    }

    // Spell out numbers to ensure a 1-2-3/one-two-three ordering didn't result from an explicit sort.
    val aircraftOneElementOne = ICAO24WithSomeText("ONE", "one")
    val aircraftOneElementTwo = ICAO24WithSomeText("ONE", "two")
    val aircraftOneElementThree = ICAO24WithSomeText("ONE", "three")
    val aircraftTwoElementOne = ICAO24WithSomeText("TWO", "one")
    val aircraftTwoElementTwo = ICAO24WithSomeText("TWO", "two")
    val aircraftTwoElementThree = ICAO24WithSomeText("TWO", "three")
    val aircraftTwoElementFour = ICAO24WithSomeText("TWO", "four")
    val aircraftThreeElementOne = ICAO24WithSomeText("THREE", "one")
    val aircraftThreeElementTwo = ICAO24WithSomeText("THREE", "two")

    "group elements by 'icao24' value, producing an AircraftSpecificData for each one; with each AircraftSpecificData's elements retaining their ordering; and with the AircraftSpecificData instances themselves ordered by when an 'icao24' value first appeared in the source data." in {
      val elements = Seq(
        aircraftOneElementOne,
        aircraftTwoElementOne,
        aircraftTwoElementTwo,
        aircraftOneElementTwo,
        aircraftThreeElementOne,
        aircraftOneElementThree,
        aircraftTwoElementThree,
        aircraftTwoElementFour,
        aircraftThreeElementTwo
      )

      val partitionedElements = partitionByICAO24(elements)

      partitionedElements.length mustEqual(3)

      partitionedElements(0).mustEqual(("ONE", Seq(
        aircraftOneElementOne,
        aircraftOneElementTwo,
        aircraftOneElementThree
      )))

      partitionedElements(1).mustEqual(("TWO", Seq(
        aircraftTwoElementOne,
        aircraftTwoElementTwo,
        aircraftTwoElementThree,
        aircraftTwoElementFour
      )))

      partitionedElements(2).mustEqual(("THREE", Seq(
        aircraftThreeElementOne,
        aircraftThreeElementTwo
      )))
    }
  }

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