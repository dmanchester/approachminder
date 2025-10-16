package com.dmanchester.approachminder.typeswithbehavior

import com.dmanchester.approachminder.SharedResources.*
import org.specs2.mutable.*

class ApproachModelSpec extends Specification {

  "newOption" should {

    val blankPosition = MeanAngleAndAltitude(PolarAngle.fromCompassDegrees(0.0), 0.0, 0.0, 0.0, 1)

    "return None on a meanTrajectory with less than two positions" in {

      val meanTrajectory = Map(
        BigDecimal("800.0") -> blankPosition
      )

      val approachModel = ApproachModel.newOption(sfoRunway28L, sfoRunway28L.opposite.thresholdCenter, meanTrajectory)
      approachModel must beNone
    }

    "set minDistanceInMeters and maxDistanceInMeters correctly" in {

      val meanTrajectory = Map(
        BigDecimal("400.0") -> blankPosition,
        BigDecimal("800.0") -> blankPosition,
        BigDecimal("500.0") -> blankPosition,
        BigDecimal("200.0") -> blankPosition
      )

      val approachModel = ApproachModel.newOption(sfoRunway28L, sfoRunway28L.opposite.thresholdCenter, meanTrajectory).get

      approachModel.minDistanceInMeters mustEqual BigDecimal("200.0")
      approachModel.maxDistanceInMeters mustEqual BigDecimal("900.0")
    }
  }

  "testSegment" should {

    "return NotContinuouslyNearing when appropriate" in {
      val result = sfoRunway28LApproachModel.testSegment(sfo28LApproachPointB, sfo28LApproachPointA)
      result mustEqual NotContinuouslyNearing
    }

    "return OutOfRange when appropriate" in {
      val result = sfoRunway28LApproachModel.testSegment(sfo28LApproachPointA, sfo28LApproachPointB)
      result mustEqual OutOfRange
    }

    "return WithinRange when appropriate" in {
      val result = sfoRunway28LApproachModel.testSegment(sfo28LApproachPointB, sfo28LApproachPointC)
      result must beLike {
        case WithinRange(deviation, distanceTestedAtInMeters) =>
          distanceTestedAtInMeters mustEqual BigDecimal("2000.0")

          deviation.angleDevInDegrees must beCloseTo(30.776488 within significantFigures)
          // At 2000 m, model's mean angle is 119.606704 deg. (See above.) Per QGIS, approach crosses that ring at
          // 150.386504 deg.; so, a QGIS-calculated deviation of 30.7798 (close to the above).

          deviation.altitudeDevInMeters must beCloseTo(17.655463 within significantFigures)
          // At 2000 m, model's mean angle is 60.0. (See above.)
          //
          // At B, was 90.0 m; at C, was 80.0 m. It looks to go down by about a quarter again as much by 2000 m; so, to
          // ~77.5 m; which is a deviation of 17.5 m. 17.655463 m is plausible as a value.
      }
    }

    "return InsufficientlyNearing when appropriate" in {
      val result = sfoRunway28LApproachModel.testSegment(sfo28LApproachPointC, sfo28LApproachPointD)
      result mustEqual InsufficientlyNearing
    }

    "return UnderRange when appropriate" in {
      val result = sfoRunway28LApproachModel.testSegment(sfo28LApproachPointD, sfo28LApproachPointE)
      result mustEqual UnderRange
    }
  }
}