package com.dmanchester.approachminder.typeswithbehavior

import com.dmanchester.approachminder.Airports.sfo
import com.dmanchester.approachminder.SharedResources.{meanTrajectoryFromSoutheast, sfo28LApproachPointB, sfo28LApproachPointC, sfoRunway28LApproachModel}
import org.specs2.mutable.*

class ApproachModelsSpec extends Specification {

  "bestFit" should {

    val sfoRunway28R = sfo.getRunwayByName("28R")
    val sfoRunway28RApproachModel = ApproachModel.newOption(sfoRunway28R, sfoRunway28R.opposite.thresholdCenter, meanTrajectoryFromSoutheast, BigDecimal("3000.0")).get
    val approachModels = ApproachModels(Seq(sfoRunway28LApproachModel, sfoRunway28RApproachModel))

    "choose the correct approach model when there are multiple candidates" in {
      val bestFit = approachModels.testForBestFit(sfo28LApproachPointB, sfo28LApproachPointC).get
      bestFit.model mustEqual sfoRunway28LApproachModel
      bestFit.distanceTestedAtInMeters mustEqual BigDecimal("2000.0")
    }

    "reach the correct conclusion when there are no candidates" in {
      approachModels.testForBestFit(sfo28LApproachPointC, sfo28LApproachPointB) must beNone
    }
  }
}