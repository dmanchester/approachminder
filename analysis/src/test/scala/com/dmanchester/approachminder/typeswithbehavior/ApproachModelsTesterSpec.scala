package com.dmanchester.approachminder.typeswithbehavior

import com.dmanchester.approachminder.data.Airports.sfo
import com.dmanchester.approachminder.data.SFOConstructs.{meanTrajectoryFromSoutheast, sfo28LApproachPointB, sfo28LApproachPointC, sfoRunway28LApproachModel}
import org.specs2.mutable.*

class ApproachModelsTesterSpec extends Specification {

  "bestFit" should {

    val sfoRunway28R = sfo.getRunwayByName("28R")
    val sfoRunway28RApproachModel = ApproachModel.newOption(sfoRunway28R, sfoRunway28R.opposite.thresholdCenter, meanTrajectoryFromSoutheast).get
    val approachModelsTester_maxThresholdDistance_10_000m = ApproachModelsTester(Seq(sfoRunway28LApproachModel, sfoRunway28RApproachModel), 10_000)
    val approachModelsTester_maxThresholdDistance_1_500m = ApproachModelsTester(Seq(sfoRunway28LApproachModel, sfoRunway28RApproachModel), 1_500)

    "choose the correct approach model when there are multiple candidates" in {
      val bestFit = approachModelsTester_maxThresholdDistance_10_000m.testForBestFit(sfo28LApproachPointB, sfo28LApproachPointC).get
      bestFit.model mustEqual sfoRunway28LApproachModel
      bestFit.distanceTestedAtInMeters mustEqual BigDecimal("2000.0")
    }

    "not test against models whose runway thresholds are too far away" in {
      val bestFit = approachModelsTester_maxThresholdDistance_1_500m.testForBestFit(sfo28LApproachPointB, sfo28LApproachPointC)
      bestFit must beNone  // From sfo28LApproachPointC, it's ~1,900 m to the threshold of Runway 28L; farther to Runway 28R.
    }

    "reach the correct conclusion when there are no candidates" in {
      val bestFit = approachModelsTester_maxThresholdDistance_10_000m.testForBestFit(sfo28LApproachPointC, sfo28LApproachPointB)
      bestFit must beNone
    }
  }
}