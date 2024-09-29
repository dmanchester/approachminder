package com.dmanchester.approachminder

import org.specs2.mutable.*

class AircraftCategorySpec extends Specification {

  "mostCommonNonBlankCategoryInNonEmptyCollection" should {

    "determine the most-common non-blank category, picking the alphabetically first one among equally common ones" in {
      val categories = Seq(NoInfoAtAll, NoADSBEmitterCategoryInfo, Small, Small, Light, Light, Large, NoInfoAtAll, NoADSBEmitterCategoryInfo, NoInfoAtAll, NoADSBEmitterCategoryInfo)
      AircraftCategory.mostCommonNonBlankCategoryInNonEmptyCollection(categories) must beSome(beEqualTo(Light))
    }

    "return `None` if all categories are blank" in {
      val categories = Seq(NoInfoAtAll, NoADSBEmitterCategoryInfo)
      AircraftCategory.mostCommonNonBlankCategoryInNonEmptyCollection(categories) must beNone
    }

    "throw if there are no categories" in {
      val categories = Seq()
      AircraftCategory.mostCommonNonBlankCategoryInNonEmptyCollection(categories) must throwA[UnsupportedOperationException]
    }
  }
}