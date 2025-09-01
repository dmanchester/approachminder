package com.dmanchester.approachminder.typeswithbehavior

import com.dmanchester.approachminder.SharedResources.significantFigures
import com.dmanchester.approachminder.typeswithoutbehavior.AngleAndAltitude
import com.dmanchester.approachminder.utils.{ApproachModeling, MathUtils}
import org.specs2.mutable.*

class MeanAngleAndAltitudeSpec extends Specification {

  "calculateDeviation" should {

    "handle the typical case: mean's standard deviations are non-zero" in {

      val meanAngleAndAltitude = ApproachModeling.meanAngleAndAltitude(Seq(
        AngleAndAltitude(PolarAngle.fromCompassDegrees(359.1), 14.1),
        AngleAndAltitude(PolarAngle.fromCompassDegrees(4.2), 16.2),
        AngleAndAltitude(PolarAngle.fromCompassDegrees(7.3), 24.3)
      )).get

      val position = AngleAndAltitude(PolarAngle.fromCompassDegrees(7.0), 20.0)
      val deviation = meanAngleAndAltitude.calculateDeviation(position)

      deviation.angleDevInDegrees must beCloseTo(3.466101 within significantFigures)
      deviation.angleStdDevs must beCloseTo(0.837131 within significantFigures)
      deviation.altitudeDevInMeters must beCloseTo(1.8 within significantFigures)
      deviation.altitudeStdDevs must beCloseTo(0.334194 within significantFigures)
    }

    val meanAngleAndAltitude_standardDeviationsZero = ApproachModeling.meanAngleAndAltitude(Seq(
      AngleAndAltitude(PolarAngle.fromCompassDegrees(359.1), 14.1),
      AngleAndAltitude(PolarAngle.fromCompassDegrees(359.1), 14.1)
    )).get

    "handle the case: mean's standard deviations are zero, sample's deviations are non-zero" in {

      val position = AngleAndAltitude(PolarAngle.fromCompassDegrees(7.0), 20.0)
      val deviation = meanAngleAndAltitude_standardDeviationsZero.calculateDeviation(position)

      deviation.angleStdDevs.isInfinity must beTrue
      deviation.altitudeStdDevs.isInfinity must beTrue
    }

    "handle the case: mean's standard deviations are zero, sample's deviations are zero" in {

      val position = AngleAndAltitude(PolarAngle.fromCompassDegrees(359.1), 14.1)
      val deviation = meanAngleAndAltitude_standardDeviationsZero.calculateDeviation(position)

      deviation.angleStdDevs mustEqual 0.0
      deviation.altitudeStdDevs mustEqual 0.0
    }
  }
}