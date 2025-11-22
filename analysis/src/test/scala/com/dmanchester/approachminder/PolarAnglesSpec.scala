package com.dmanchester.approachminder

import com.dmanchester.approachminder.SharedResources.significantFigures
import com.dmanchester.approachminder.typeswithbehavior.PolarAngle
import org.specs2.mutable.*

class PolarAnglesSpec extends Specification {

  "circularMeanAndStdDevDegrees" should {

    "calculate the correct values" in {

      val angles = Seq(PolarAngle.fromCompassDegrees(359.1),
        PolarAngle.fromCompassDegrees(4.2),
        PolarAngle.fromCompassDegrees(7.3)
      )

      val actual = PolarAngles.circularMeanAndStdDevDegrees(angles)

      actual._1.asCompassDegrees must beCloseTo(3.533899 within significantFigures)
      actual._2 must beCloseTo(4.140451 within significantFigures)
    }

    "throw on less than two angles" in {
      val angles = Seq(PolarAngle.fromCompassDegrees(359.1))
      PolarAngles.circularMeanAndStdDevDegrees(angles) must throwAn[IllegalArgumentException]
    }
  }
}