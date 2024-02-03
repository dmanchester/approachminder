package com.dmanchester.approachminder

import SharedResources.{mockApproachDistributions, sfoCalculator, significantFigures}

import org.specs2.mutable._

class ApproachModelsSpec extends Specification {

  // The data in these tests relies on two airports and three points:
  //
  //   |               |
  //   |   A           |
  //   |               |
  //   |   B       C   |
  //   |               |
  //  LEFT           RIGHT
  // AIRPORT        AIRPORT
  //
  // Each airport's approach is purely southerly (the vertical lines). The points are positioned such that:
  //
  // * Directed segment BC: Continuously approaches only the right airport.
  // * Directed segment AB: Continuously approaches both airports; is closer to the left one.
  // * Directed segment BA: Continuously approaches neither airport.

  val simpleSouthboundApproachModel = Map(  // distance, angle, altitude
    BigDecimal("99000.0") -> (0.0, 100.0),
    BigDecimal("100.0") -> (0.0, 100.0)
  )

  val approachModelLeft = ApproachModel.newOption(LongLat(-122.1, 38.0), mockApproachDistributions(simpleSouthboundApproachModel), sfoCalculator).get
  val approachModelRight = ApproachModel.newOption(LongLat(-121.8, 38.0), mockApproachDistributions(simpleSouthboundApproachModel), sfoCalculator).get

  val approachModels = ApproachModels(Seq(approachModelLeft, approachModelRight))

  val pointA = LongLatAlt(-122.0, 38.2, 200.0)
  val pointB = LongLatAlt(-122.0, 38.1, 200.0)
  val pointC = LongLatAlt(-121.9, 38.1, 200.0)

  "bestFit" should {

    "find the correct approach when there's only one candidate" in {
      val (model, appliedDistributionInMeters, deviation) = approachModels.bestFit(pointB, pointC).get
      model must beEqualTo(approachModelRight)
    }

    "choose the correct approach when there are multiple candidates" in {
      val (model, appliedDistributionInMeters, deviation) = approachModels.bestFit(pointA, pointB).get
      model must beEqualTo(approachModelLeft)
    }

    "reach the correct conclusion when there are no candidates" in {
      val result = approachModels.bestFit(pointB, pointA)
      result must beNone
    }
  }
}