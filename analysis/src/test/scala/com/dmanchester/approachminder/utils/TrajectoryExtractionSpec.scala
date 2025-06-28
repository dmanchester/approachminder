package com.dmanchester.approachminder.utils

import com.dmanchester.approachminder.testingtypes.ICAO24WithSomeText
import com.dmanchester.approachminder.utils.TrajectoryExtraction.partitionByICAO24
import com.dmanchester.approachminder.typeswithoutbehavior.HasICAO24
import org.specs2.mutable.*

class TrajectoryExtractionSpec extends Specification {

  "partitionByICAO24" should {

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

    "group elements by ICAO value, with each group's elements retaining their ordering; and with the groups themselves ordered by when an ICAO24 value first appeared in the input data" in {
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

      partitionedElements.length mustEqual 3

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
}