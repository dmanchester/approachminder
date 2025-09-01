package com.dmanchester.approachminder.utils

import com.dmanchester.approachminder.SharedResources.significantFigures
import com.dmanchester.approachminder.utils.MathUtils.{divideWithAlt0_0Handling, interpolateScalar, roundDownToNearestMultiple}
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
}