package com.dmanchester.approachminder.utils

import com.dmanchester.approachminder.SharedResources.significantFigures
import com.dmanchester.approachminder.typeswithbehavior.PolarAngle
import com.dmanchester.approachminder.typeswithoutbehavior.AngleAndAltitude
import org.specs2.mutable.*

class ApproachModelingSpec extends Specification {

  "calculateMeanAngleAndAltitude" should {

    val onePosition = Seq(
      AngleAndAltitude(PolarAngle.fromCompassDegrees(359.1), 14.1)
    )

    val threePositions = onePosition :++ Seq(
      AngleAndAltitude(PolarAngle.fromCompassDegrees(4.2), 16.2),
      AngleAndAltitude(PolarAngle.fromCompassDegrees(7.3), 24.3)
    )

    "handle a typical case" in {

      val meanAngleAndAltitude = ApproachModeling.calculateMeanAngleAndAltitude(threePositions).get

      meanAngleAndAltitude.angle.asCompassDegrees must beCloseTo(3.533899 within significantFigures)
      meanAngleAndAltitude.angleStdDevInDegrees must beCloseTo(4.140451 within significantFigures)
      meanAngleAndAltitude.altitudeInMeters must beCloseTo(18.2 within significantFigures)
      meanAngleAndAltitude.altitudeStdDevInMeters must beCloseTo(5.386093 within significantFigures)
      meanAngleAndAltitude.positionsCount mustEqual 3
    }

    "throw on insufficient positions" in {
      ApproachModeling.calculateMeanAngleAndAltitude(onePosition) must beNone
    }
  }
}