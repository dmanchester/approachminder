package com.dmanchester.approachminder.utils

import com.dmanchester.approachminder.SharedResources.significantFigures
import com.dmanchester.approachminder.utils.MathUtils.interpolateScalar
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
}