package com.dmanchester.approachminder.utils

import com.dmanchester.approachminder.typeswithoutbehavior.*
import org.specs2.mutable.*

class CollectionUtilsSpec extends Specification {

  "mostCommonNonBlankCategoryInNonEmptyCollection" should {

    "determine the most-common non-blank category" in {
      val categories = Seq(NoInfoAtAll, Small, NoADSBEmitterCategoryInfo, Small, Small, Light, Light, Large, NoInfoAtAll, NoADSBEmitterCategoryInfo, NoInfoAtAll, NoADSBEmitterCategoryInfo)
      CollectionUtils.mostCommonNonBlankCategoryInNonEmptyCollection(categories) must beSome(beEqualTo(Small))
    }

    "determine the most-common non-blank category, picking the alphabetically first one among equally common ones" in {
      val categories = Seq(NoInfoAtAll, NoADSBEmitterCategoryInfo, Small, Small, Light, Light, Large, NoInfoAtAll, NoADSBEmitterCategoryInfo, NoInfoAtAll, NoADSBEmitterCategoryInfo)
      CollectionUtils.mostCommonNonBlankCategoryInNonEmptyCollection(categories) must beSome(beEqualTo(Light))
    }

    "return None if all categories are blank" in {
      val categories = Seq(NoInfoAtAll, NoADSBEmitterCategoryInfo)
      CollectionUtils.mostCommonNonBlankCategoryInNonEmptyCollection(categories) must beNone
    }

    "throw if there are no categories" in {
      val categories = Seq.empty
      CollectionUtils.mostCommonNonBlankCategoryInNonEmptyCollection(categories) must throwAn[UnsupportedOperationException]
    }
  }
}