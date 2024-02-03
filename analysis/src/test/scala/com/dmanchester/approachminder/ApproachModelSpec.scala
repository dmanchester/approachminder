package com.dmanchester.approachminder

import SharedResources.{mockApproachDistributions, sfoCalculator, significantFigures}

import org.specs2.mutable._

class ApproachModelSpec extends Specification {

  val simpleApproachModel = Map(  // distance, angle, altitude
    BigDecimal("3000.0") -> (55.0, 1600.0),
    BigDecimal("2000.0") -> (50.0, 1100.0),
    BigDecimal("1000.0") -> (45.0, 500.0)
  )

  val trajectoryCylindricalCoordinates = Seq(  // distance, angle, altitude
    (5000.0, 37.0, 2700.0),
    (4000.0, 39.0, 1950.0),
    (2900.0, 41.0, 1500.0),
    (2100.0, 43.0, 1040.0),
    (900.0, 47.0, 550.0)
  )

  val referencePoint = LongLat(-122, 38)

  def toLongLatAlt(cylindricalCoordinates: Seq[(Double, Double, Double)], referencePoint: HasLongLat, calculator: GeographicCalculator): Seq[LongLatAlt] = {

    cylindricalCoordinates.map { case (distanceMeters, polarAngleCompassDegrees, altitudeMeters) =>
      val longLat = calculator.pointAtAngleAndDistance(referencePoint, PolarAngle.fromCompassDegrees(polarAngleCompassDegrees), distanceMeters)
      LongLatAlt(longLat.longitude, longLat.latitude, altitudeMeters)
    }
  }

  val approachModel = ApproachModel.newOption(referencePoint, mockApproachDistributions(simpleApproachModel), sfoCalculator).get

  val trajectory = toLongLatAlt(trajectoryCylindricalCoordinates, referencePoint, sfoCalculator)

  "test" should {

    "return NotContinuouslyNearing when appropriate" in {
      val testResult = approachModel.test(trajectory(1), trajectory(0))
      testResult must beEqualTo(NotContinuouslyNearing)
    }

    "return ContinuouslyNearingButOutOfRange when appropriate" in {
      val testResult = approachModel.test(trajectory(0), trajectory(1))
      testResult must beEqualTo(ContinuouslyNearingButOutOfRange)
    }

    "return WithinRange when the segment continuously nears the reference point and the current point lies between two model distances; further, the method should choose the previous (i.e., already-crossed) model distance when it's closer to the current point than the next distance is" in {
      val testResult = approachModel.test(trajectory(1), trajectory(2))
      testResult must beLike {
        case WithinRange(appliedDistributionInMeters, deviation) => {
          appliedDistributionInMeters must beEqualTo(BigDecimal("3000.0"))
          deviation.angleDevDegrees must beCloseTo(-14.242848 within significantFigures)
          deviation.altitudeDevMeters must beCloseTo(-59.0111355 within significantFigures)
        }
      }
    }

    "return WithinRange when the segment continuously nears the reference point and the current point lies between two model distances; further, the method should choose the next (i.e., not-yet-crossed) model distance when it's closer to the current point than the previous distance is" in {
      val testResult = approachModel.test(trajectory(2), trajectory(3))
      testResult must beLike {
        case WithinRange(appliedDistributionInMeters, deviation) => {
          appliedDistributionInMeters must beEqualTo(BigDecimal("2000.0"))
          deviation.angleDevDegrees must beCloseTo(-6.636623 within significantFigures)
          deviation.altitudeDevMeters must beCloseTo(-117.650434 within significantFigures)
        }
      }
    }

    "return WithinRange when the segment continuously nears the reference point and the current point lies closer than the closest model distance; further, the method should choose that model distance" in {
      val testResult = approachModel.test(trajectory(3), trajectory(4))
      testResult must beLike {
        case WithinRange(appliedDistributionInMeters, deviation) => {
          appliedDistributionInMeters must beEqualTo(BigDecimal("1000.0"))
          deviation.angleDevDegrees must beCloseTo(1.298083 within significantFigures)
          deviation.altitudeDevMeters must beCloseTo(90.977375 within significantFigures)
        }
      }
    }
  }
}