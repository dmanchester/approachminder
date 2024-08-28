package com.dmanchester.approachminder

import org.specs2.mutable.*

class AircraftSpecificDataSpec extends Specification {

  "createMultiple" should {

    "handle an empty Seq" in {
      val elements = Seq.empty[HasICAO24]
      val multipleAircraftSpecificData = AircraftSpecificData.createMultiple(elements)
      multipleAircraftSpecificData must empty
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

      val multipleAircraftSpecificData = AircraftSpecificData.createMultiple(elements)

      multipleAircraftSpecificData.length mustEqual(3)

      multipleAircraftSpecificData(0).seq.mustEqual(Seq(
        aircraftOneElementOne,
        aircraftOneElementTwo,
        aircraftOneElementThree
      ))

      multipleAircraftSpecificData(1).seq.mustEqual(Seq(
        aircraftTwoElementOne,
        aircraftTwoElementTwo,
        aircraftTwoElementThree,
        aircraftTwoElementFour
      ))

      multipleAircraftSpecificData(2).seq.mustEqual(Seq(
        aircraftThreeElementOne,
        aircraftThreeElementTwo
      ))
    }
  }
}