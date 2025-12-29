package com.dmanchester.approachminder.utils

import com.dmanchester.approachminder.SharedResources.significantFigures
import com.dmanchester.approachminder.typeswithbehavior.PolarAngle
import com.dmanchester.approachminder.utils.MathUtils.*
import org.specs2.mutable.*

class MathUtilsSpec extends Specification {

  "interpolateScalar" should {
    "interpolate" in {
      interpolateScalar(-0.5, 9.5, 0.95) must beCloseTo(9.0 within significantFigures)
      // The distance from -0.5 to 9.5 is 10.0.
      // 95% of 10.0 is 9.5.
      // -0.5 + 9.5 = 9.0.
    }
  }

  "roundDownToNearestMultiple" should {
    "handle a typical case" in {
      roundDownToNearestMultiple(BigDecimal("7.7"), BigDecimal("0.5")) mustEqual BigDecimal("7.5")
    }

    "handle the case that the value is a multiple of the step size" in {
      roundDownToNearestMultiple(BigDecimal("4.4"), BigDecimal("1.1")) mustEqual BigDecimal("4.4")
    }
  }

  "divideWithAlt0_0Handling" should {

    "perform standard division for most cases" in {
      divideWithAlt0_0Handling(6.0, 3.0) mustEqual 2.0
    }

    "return 0.0 for 0.0 / 0.0" in {
      divideWithAlt0_0Handling(0.0, 0.0) mustEqual 0.0
    }
  }

  "hypotenuseLength" should {
    "calculate length" in {
      hypotenuseLength(6.0, 7.0) must beCloseTo(9.219544 within significantFigures)
    }
  }

  "isoscelesBaseLength" should {

    "handle positive angles" in {
      isoscelesBaseLength(10.5, 100.6) must beCloseTo(18.410126 within significantFigures)
    }

    "handle negative angles" in {
      isoscelesBaseLength(-10.5, 100.6) must beCloseTo(-18.410126 within significantFigures)
    }

    "handle an angle of 0" in {
      isoscelesBaseLength(0, 99.1) must beCloseTo(0.0 within significantFigures)
    }
  }

  "circularMeanAndStdDevDegrees" should {

    "calculate the correct values" in {

      val angles = Seq(PolarAngle.fromCompassDegrees(359.1),
        PolarAngle.fromCompassDegrees(4.2),
        PolarAngle.fromCompassDegrees(7.3)
      )

      val actual = MathUtils.circularMeanAndStdDevDegrees(angles)

      actual._1.asCompassDegrees must beCloseTo(3.533899 within significantFigures)
      actual._2 must beCloseTo(4.140451 within significantFigures)
    }

    "throw on less than two angles" in {
      val angles = Seq(PolarAngle.fromCompassDegrees(359.1))
      MathUtils.circularMeanAndStdDevDegrees(angles) must throwAn[IllegalArgumentException]
    }
  }
}