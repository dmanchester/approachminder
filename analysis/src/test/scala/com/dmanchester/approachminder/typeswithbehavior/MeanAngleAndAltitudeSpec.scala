package com.dmanchester.approachminder.typeswithbehavior

import com.dmanchester.approachminder.SharedResources.significantFigures
import com.dmanchester.approachminder.typeswithoutbehavior.AngleAndAltitude
import com.dmanchester.approachminder.utils.MathUtils
import org.specs2.mutable.*

class MeanAngleAndAltitudeSpec extends Specification {

  "calculateDeviation" should {

    "calculate correctly" in {

      val positionsForMean = Seq(
        AngleAndAltitude(PolarAngle.fromCompassDegrees(359.1), 14.1),
        AngleAndAltitude(PolarAngle.fromCompassDegrees(4.2), 16.2),
        AngleAndAltitude(PolarAngle.fromCompassDegrees(7.3), 24.3)
      )

      val meanAngleAndAltitude = MathUtils.calculateMeanAngleAndAltitude(positionsForMean)

      val position = AngleAndAltitude(PolarAngle.fromCompassDegrees(7.0), 20.0)
      val deviation = meanAngleAndAltitude.get.calculateDeviation(position)

      deviation.angleDevInDegrees must beCloseTo(3.466101 within significantFigures)
      deviation.angleStdDevs must beCloseTo(0.837131 within significantFigures)
      deviation.altitudeDevInMeters must beCloseTo(1.8 within significantFigures)
      deviation.altitudeStdDevs must beCloseTo(0.334194 within significantFigures)
    }
  }
}