package com.dmanchester.approachminder

import org.specs2.mutable.*
import SharedResources.*
import com.dmanchester.approachminder.Utils.{hypotenuseLength, isoscelesBaseLength, mostCommonString}

class UtilsSpec extends Specification {

  "isoscelesBaseLength" should {

    "handle positive angles" in {
      isoscelesBaseLength(10.5, 100.6) must beCloseTo(18.410126 within significantFigures)
    }

    "handle negative angles" in {
      isoscelesBaseLength(-10.5, 100.6) must beCloseTo(-18.410126 within significantFigures)
    }

    "handle an angle of 0" in {
      isoscelesBaseLength(0, 99.1) must beCloseTo(0.0 within significantFigures)
    }
  }

  "hypotenuseLength" should {
    "calculate length" in {
      hypotenuseLength(6.0, 7.0) must beCloseTo(9.219544 within significantFigures)
    }
  }

  "mostCommonString" should {

    "determine the most-common string, picking the alphabetically first one among equally common ones" in {
      val strings = Seq("Cherry", "Apple", "Banana", "Apple", "Cherry")
      mostCommonString(strings) mustEqual(Some("Apple"))
    }

    "return None if there are no strings" in {
      val strings = Seq()
      mostCommonString(strings) must beNone
    }
  }
}