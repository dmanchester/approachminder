package com.dmanchester.approachminder

import com.dmanchester.approachminder.SharedResources.*
import com.dmanchester.approachminder.Utils.{hypotenuseLength, mostCommonString}
import org.specs2.mutable.*

class UtilsSpec extends Specification {

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