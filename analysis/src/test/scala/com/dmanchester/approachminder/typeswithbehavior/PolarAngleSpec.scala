package com.dmanchester.approachminder.typeswithbehavior

import com.dmanchester.approachminder.SharedResources.significantFigures
import org.specs2.mutable.*

import scala.math.Pi

class PolarAngleSpec extends Specification {

  "fromCompassDegrees (pseudo-constructor) in collaboration with compassDegrees" should {

    "normalize values where appropriate" in {
      PolarAngle.fromCompassDegrees(45.0).asCompassDegrees must beCloseTo(45.0 within significantFigures)  // no normalization needed
      PolarAngle.fromCompassDegrees(495.1).asCompassDegrees must beCloseTo(135.1 within significantFigures)
      PolarAngle.fromCompassDegrees(-134.8).asCompassDegrees must beCloseTo(225.2 within significantFigures)
      PolarAngle.fromCompassDegrees(-404.7).asCompassDegrees must beCloseTo(315.3 within significantFigures)
    }
  }

  "fromRadians (pseudo-constructor) in collaboration with compassDegrees" should {

    "convert values" in {

      PolarAngle.fromRadians(0).asCompassDegrees must beCloseTo(90.0 within significantFigures)
      PolarAngle.fromRadians(Pi).asCompassDegrees must beCloseTo(270.0 within significantFigures)

      // Examine values at the Pi/2 discontinuity.
      PolarAngle.fromRadians(Pi/2).asCompassDegrees must beCloseTo(0.0 within significantFigures)
      PolarAngle.fromRadians(Pi/2 + 0.000001).asCompassDegrees must beCloseTo(360.0 within significantFigures)

      // Examine values at the -3*Pi/2 discontinuity.
      PolarAngle.fromRadians(-3*Pi/2).asCompassDegrees must beCloseTo(0.0 within significantFigures)
      PolarAngle.fromRadians(-3*Pi/2 + 0.000001).asCompassDegrees must beCloseTo(360.0 within significantFigures)
    }

    "normalize values" in {
      PolarAngle.fromRadians(4*Pi).asCompassDegrees must beCloseTo(90.0 within significantFigures)
      PolarAngle.fromRadians(-3*Pi).asCompassDegrees must beCloseTo(270.0 within significantFigures)
    }
  }

  "fromCompassDegrees (pseudo-constructor) in collaboration with radians" should {

    "convert values" in {

      PolarAngle.fromCompassDegrees(0.0).asRadians must beCloseTo(Pi / 2 within significantFigures)
      PolarAngle.fromCompassDegrees(90.0).asRadians must beCloseTo(0.0 within significantFigures)
      PolarAngle.fromCompassDegrees(180.0).asRadians must beCloseTo(-Pi / 2 within significantFigures)

      // Examine values at the 270 deg. discontinuity.
      PolarAngle.fromCompassDegrees(270.0 - 0.000001).asRadians must beCloseTo(-Pi within significantFigures)
      PolarAngle.fromCompassDegrees(270.0).asRadians must beCloseTo(Pi within significantFigures)

      PolarAngle.fromCompassDegrees(360.0 - 0.000001).asRadians must beCloseTo(Pi / 2 within significantFigures)
    }

    "minusAsDegrees" should {

      "handle simple cases (and respect decimal places)" in {
        PolarAngle.fromCompassDegrees(135.1).minusAsDegrees(PolarAngle.fromCompassDegrees(89.9)) must beCloseTo(45.2 within significantFigures)
      }

      "handle the case where a negative result is expected" in {
        PolarAngle.fromCompassDegrees(280.0).minusAsDegrees(PolarAngle.fromCompassDegrees(90.0)) must beCloseTo(-170.0 within significantFigures)
      }

      "handle the case where the shortest arc passes due north" in {
        PolarAngle.fromCompassDegrees(1.0).minusAsDegrees(PolarAngle.fromCompassDegrees(359.0)) must beCloseTo(2.0 within significantFigures)
      }
    }
  }
}