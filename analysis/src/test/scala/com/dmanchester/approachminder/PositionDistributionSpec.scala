package com.dmanchester.approachminder

import com.dmanchester.approachminder.SharedResources.significantFigures
import org.specs2.mutable._

class PositionDistributionSpec extends Specification {

  val onePosition = Seq(
    AngleAndAltitude(PolarAngle.fromCompassDegrees(359.1), 14.1)
  )

  val threePositions = onePosition :++ Seq(
    AngleAndAltitude(PolarAngle.fromCompassDegrees(4.2), 16.2),
    AngleAndAltitude(PolarAngle.fromCompassDegrees(7.3), 24.3)
  )

  "fromData (pseudo-constructor)" should {

    "initialize an instance with the correct means and standard deviations" in {

      val positionDistribution = PositionDistribution.fromDataOption(threePositions).get

      positionDistribution.angleMean.toCompassDegrees must beCloseTo(3.533899 within significantFigures)
      positionDistribution.angleStdDevDegrees must beCloseTo(4.140451 within significantFigures)
      positionDistribution.altitudeMeanMeters must beCloseTo(18.2 within significantFigures)
      positionDistribution.altitudeStdDevMeters must beCloseTo(5.386093 within significantFigures)
      positionDistribution.valuesIncluded mustEqual 3
    }

    "refuse to initialize an instance from less than two positions" in {

      val positionDistribution = PositionDistribution.fromDataOption(onePosition)

      positionDistribution must beNone
    }
  }

  "deviation" should {

    "calculate a passed-in position's deviation from the distribution" in {

      val positionDistribution = PositionDistribution.fromDataOption(threePositions).get

      val position = AngleAndAltitude(PolarAngle.fromCompassDegrees(7.0), 20.0)
      val deviation = positionDistribution.deviation(position)

      deviation.angleDevDegrees must beCloseTo(3.466101 within significantFigures)
      deviation.angleStdDevs must beCloseTo(0.837131 within significantFigures)
      deviation.altitudeDevMeters must beCloseTo(1.8 within significantFigures)
      deviation.altitudeStdDevs must beCloseTo(0.334194 within significantFigures)
      deviation.normalizedEuclideanDistance must beCloseTo(0.901374 within significantFigures)
    }
  }
}