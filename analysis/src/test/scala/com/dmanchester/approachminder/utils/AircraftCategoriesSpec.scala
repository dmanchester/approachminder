package com.dmanchester.approachminder.utils

import com.dmanchester.approachminder.typeswithoutbehavior.*
import org.specs2.mutable.*

class AircraftCategoriesSpec extends Specification {

  "mostCommonNonBlankCategoryInNonEmptyCollection" should {

    "determine the most-common non-blank category, picking the alphabetically first one among equally common ones" in {
      val categories = Seq(NoInfoAtAll, NoADSBEmitterCategoryInfo, Small, Small, Light, Light, Large, NoInfoAtAll, NoADSBEmitterCategoryInfo, NoInfoAtAll, NoADSBEmitterCategoryInfo)
      AircraftCategories.mostCommonNonBlankCategoryInNonEmptyCollection(categories) must beSome(beEqualTo(Light))
    }

    "return `None` if all categories are blank" in {
      val categories = Seq(NoInfoAtAll, NoADSBEmitterCategoryInfo)
      AircraftCategories.mostCommonNonBlankCategoryInNonEmptyCollection(categories) must beNone
    }

    "throw if there are no categories" in {
      val categories = Seq()
      AircraftCategories.mostCommonNonBlankCategoryInNonEmptyCollection(categories) must throwA[UnsupportedOperationException]
    }
  }
}