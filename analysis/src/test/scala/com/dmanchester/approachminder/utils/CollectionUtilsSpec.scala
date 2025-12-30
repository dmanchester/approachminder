package com.dmanchester.approachminder.utils

import com.dmanchester.approachminder.typeswithoutbehavior.*
import org.specs2.mutable.*

class CollectionUtilsSpec extends Specification {

  "mostCommonNonBlankCategoryInNonEmptyCollection" should {

    "ignore blank categories that are more common" in {
      val categories = Seq(Light, NoInfoAtAll, Small, NoInfoAtAll, Small, NoInfoAtAll)
      CollectionUtils.mostCommonNonBlankCategoryInNonEmptyCollection(categories) must beSome(beEqualTo(Small))
    }

    "pick the alphabetically first category among equally common ones" in {
      val categories = Seq(Light, Small, Small, Light)
      CollectionUtils.mostCommonNonBlankCategoryInNonEmptyCollection(categories) must beSome(beEqualTo(Light))
    }

    "return None if all categories are blank" in {
      val categories = Seq(NoInfoAtAll, NoADSBEmitterCategoryInfo)
      CollectionUtils.mostCommonNonBlankCategoryInNonEmptyCollection(categories) must beNone
    }

    "throw if there are no categories" in {
      val categories = Seq.empty
      CollectionUtils.mostCommonNonBlankCategoryInNonEmptyCollection(categories) must throwAn[IllegalArgumentException]
    }
  }
}