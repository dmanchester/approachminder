package com.dmanchester.approachminder.typeswithbehavior

import com.dmanchester.approachminder.typeswithbehavior.AircraftCategory.mostCommonNonBlankCategory
import org.specs2.mutable.*

class AircraftCategorySpec extends Specification {

  "mostCommonNonBlankCategory" should {

    "ignore blank categories that are more common" in {
      val categories = Seq(Light, NoInfoAtAll, Small, NoInfoAtAll, Small, NoInfoAtAll)
      mostCommonNonBlankCategory(categories) must beSome(beEqualTo(Small))
    }

    "pick the alphabetically first category among equally common ones" in {
      val categories = Seq(Light, Small, Small, Light)
      mostCommonNonBlankCategory(categories) must beSome(beEqualTo(Light))
    }

    "return None if all categories are blank" in {
      val categories = Seq(NoInfoAtAll, NoADSBEmitterCategoryInfo)
      mostCommonNonBlankCategory(categories) must beNone
    }

    "throw if there are no categories" in {
      val categories = Seq.empty
      mostCommonNonBlankCategory(categories) must throwAn[IllegalArgumentException]
    }
  }
}