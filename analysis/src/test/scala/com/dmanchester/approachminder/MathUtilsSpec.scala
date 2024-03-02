package com.dmanchester.approachminder

import org.specs2.mutable._
import SharedResources._
import com.dmanchester.approachminder.MathUtils.{interpolateScalar, isBetween, isoscelesBaseLength}

import scala.math.Pi

class MathUtilsSpec extends Specification {

  "isBetween" should {

    "handle 'a' less than 'b'" in {
      isBetween(2.1, 1.1, 3.1) must beTrue
      isBetween(0.1, 1.1, 3.1) must beFalse
    }

    "handle 'b' less than 'a'" in {
      isBetween(2.1, 3.1, 1.1) must beTrue
      isBetween(0.1, 3.1, 1.1) must beFalse
    }

    "handle negative and mixed values" in {
      isBetween(-1.1, -2.1, 2.1) must beTrue
      isBetween(1.1, -2.1, 2.1) must beTrue
      isBetween(3.1, -2.1, 2.1) must beFalse
    }
  }

  "interpolateScalar" should {

    "interpolate" in {
      interpolateScalar(-0.5, 9.5, 0.95) must beCloseTo(9.0 within significantFigures)
      // The distance from -0.5 to 9.5 is 10.0.
      // 95% of 10.0 is 9.5.
      // -0.5 + 9.5 = 9.0.
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
}