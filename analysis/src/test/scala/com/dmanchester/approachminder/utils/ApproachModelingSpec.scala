package com.dmanchester.approachminder.utils

import com.dmanchester.approachminder.resources.TestHelpers.significantFigures
import com.dmanchester.approachminder.typeswithbehavior.PolarAngle
import com.dmanchester.approachminder.typeswithoutbehavior.AngleAndAltitude
import org.specs2.mutable.*

class ApproachModelingSpec extends Specification {

  "meanAngleAndAltitude" should {

    val onePosition = Seq(
      AngleAndAltitude(PolarAngle.fromCompassDegrees(359.1), 14.1)
    )

    val threePositions = onePosition :++ Seq(
      AngleAndAltitude(PolarAngle.fromCompassDegrees(4.2), 16.2),
      AngleAndAltitude(PolarAngle.fromCompassDegrees(7.3), 24.3)
    )

    "handle a typical case" in {

      val meanAngleAndAltitude = ApproachModeling.meanAngleAndAltitude(threePositions).get

      meanAngleAndAltitude.angle.asCompassDegrees must beCloseTo(3.533899 within significantFigures)
      meanAngleAndAltitude.angleStdDevInDegrees must beCloseTo(4.140451 within significantFigures)
      meanAngleAndAltitude.altitudeInMeters must beCloseTo(18.2 within significantFigures)
      meanAngleAndAltitude.altitudeStdDevInMeters must beCloseTo(5.386093 within significantFigures)
      meanAngleAndAltitude.positionsCount mustEqual 3
    }

    "return None on insufficient positions" in {
      ApproachModeling.meanAngleAndAltitude(onePosition) must beNone
    }
  }

  "meanTrajectory" should {

    "calculate mean positions at all distances where at least two positions are available" in {

      // The three trajectories for this test are laid out as follows (format: angle, altitude),
      // giving rise to two mean positions (format: mean angle (angle variance), mean altitude
      // (altitude variance)):
      //
      // Distance    Traj. A     Traj. B     Traj. C            Mean Position        Positions
      // --------   ---------   ---------   ---------       ----------------------   ---------
      //   1.0      1.0, 10.0       X           X
      //   1.5      2.0, 12.0   3.0, 12.0       X      ==>  2.5 (0.5), 12.0 (0.0)        2
      //   2.0      3.0, 14.0   6.0, 16.0   9.0, 24.0       6.0 (9.0), 18.0 (28.0)       3
      //   2.5         X        9.0, 20.0       X

      val bd_1_0 = BigDecimal("1.0")  // "bd" = "BigDecimal"
      val bd_1_5 = BigDecimal("1.5")
      val bd_2_0 = BigDecimal("2.0")
      val bd_2_5 = BigDecimal("2.5")

      val trajectories = Seq(
        Map(bd_1_0 -> AngleAndAltitude(PolarAngle.fromCompassDegrees(1.0), 10.0), bd_1_5 -> AngleAndAltitude(PolarAngle.fromCompassDegrees(2.0), 12.0), bd_2_0 -> AngleAndAltitude(PolarAngle.fromCompassDegrees(3.0), 14.0)),
        Map(bd_1_5 -> AngleAndAltitude(PolarAngle.fromCompassDegrees(3.0), 12.0), bd_2_0 -> AngleAndAltitude(PolarAngle.fromCompassDegrees(6.0), 16.0), bd_2_5 -> AngleAndAltitude(PolarAngle.fromCompassDegrees(9.0), 20.0)),
        Map(bd_2_0 -> AngleAndAltitude(PolarAngle.fromCompassDegrees(9.0), 24.0))
      )

      val meanTrajectory = ApproachModeling.meanTrajectory(trajectories)

      meanTrajectory.size mustEqual 2

      meanTrajectory(bd_1_5).angle.asCompassDegrees must beCloseTo(2.5 within significantFigures)
      meanTrajectory(bd_1_5).angleStdDevInDegrees must beCloseTo(0.707107 within significantFigures)
      meanTrajectory(bd_1_5).altitudeInMeters must beCloseTo(12.0 within significantFigures)
      meanTrajectory(bd_1_5).altitudeStdDevInMeters must beCloseTo(0.0 within significantFigures)
      meanTrajectory(bd_1_5).positionsCount mustEqual 2

      meanTrajectory(bd_2_0).angle.asCompassDegrees must beCloseTo(6.0 within significantFigures)
      meanTrajectory(bd_2_0).angleStdDevInDegrees must beCloseTo(3.0 within significantFigures)
      meanTrajectory(bd_2_0).altitudeInMeters must beCloseTo(18.0 within significantFigures)
      meanTrajectory(bd_2_0).altitudeStdDevInMeters must beCloseTo(5.291503 within significantFigures)
      meanTrajectory(bd_2_0).positionsCount mustEqual 3
    }
  }
}