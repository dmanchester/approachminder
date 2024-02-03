package com.dmanchester.approachminder

import SharedResources.significantFigures

import org.specs2.mutable._

class PolarAnglesSpec extends Specification {

  val angles = Seq(PolarAngle.fromCompassDegrees(359.1),
    PolarAngle.fromCompassDegrees(4.2),
    PolarAngle.fromCompassDegrees(7.3)
  )

  val circularMeanExpected = 3.533899
  val stdDevDegreesExpected = 4.140451

  "circularMean" should {
    "calculate the correct value" in {
      PolarAngles.circularMean(angles).toCompassDegrees must beCloseTo(circularMeanExpected within significantFigures)
    }
  }

  "circularMean" should {
    "calculate the correct values" in {
      val actual = PolarAngles.circularMeanAndStdDevDegrees(angles)
      actual._1.toCompassDegrees must beCloseTo(circularMeanExpected within significantFigures)
      actual._2 must beCloseTo(stdDevDegreesExpected within significantFigures)
    }
  }
}