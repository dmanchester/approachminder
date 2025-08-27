package com.dmanchester.approachminder.utils

import com.dmanchester.approachminder.SharedResources.significantFigures
import com.dmanchester.approachminder.typeswithbehavior.PolarAngle
import com.dmanchester.approachminder.typeswithoutbehavior.AngleAndAltitude
import com.dmanchester.approachminder.utils.MathUtils.{interpolateScalar, roundDownToNearestMultiple}
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

  "calculateMeanAngleAndAltitude" should {

    val onePosition = Seq(
      AngleAndAltitude(PolarAngle.fromCompassDegrees(359.1), 14.1)
    )

    val threePositions = onePosition :++ Seq(
      AngleAndAltitude(PolarAngle.fromCompassDegrees(4.2), 16.2),
      AngleAndAltitude(PolarAngle.fromCompassDegrees(7.3), 24.3)
    )

    "handle a typical case" in {

      val meanAngleAndAltitude = MathUtils.calculateMeanAngleAndAltitude(threePositions).get

      meanAngleAndAltitude.angle.asCompassDegrees must beCloseTo(3.533899 within significantFigures)
      meanAngleAndAltitude.angleStdDevInDegrees must beCloseTo(4.140451 within significantFigures)
      meanAngleAndAltitude.altitudeInMeters must beCloseTo(18.2 within significantFigures)
      meanAngleAndAltitude.altitudeStdDevInMeters must beCloseTo(5.386093 within significantFigures)
      meanAngleAndAltitude.positionsCount mustEqual 3
    }

    "throw on insufficient positions" in {
      MathUtils.calculateMeanAngleAndAltitude(onePosition) must beNone
    }
  }
}