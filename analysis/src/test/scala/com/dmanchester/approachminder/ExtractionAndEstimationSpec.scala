package com.dmanchester.approachminder

import com.dmanchester.approachminder.SharedResources.*
import com.dmanchester.approachminder.typeswithbehavior.PolarAngle
import org.specs2.mutable.*

class ExtractionAndEstimationSpec extends Specification {

  "meanTrajectories" should {

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

      // FIXME Put in real altitude values; and check them at bottom

      val trajectories = Seq(
        Map(bd_1_0 -> AngleAndAltitude(PolarAngle.fromCompassDegrees(1.0), 10.0), bd_1_5 -> AngleAndAltitude(PolarAngle.fromCompassDegrees(2.0), 12.0), bd_2_0 -> AngleAndAltitude(PolarAngle.fromCompassDegrees(3.0), 14.0)),
        Map(bd_1_5 -> AngleAndAltitude(PolarAngle.fromCompassDegrees(3.0), 12.0), bd_2_0 -> AngleAndAltitude(PolarAngle.fromCompassDegrees(6.0), 16.0), bd_2_5 -> AngleAndAltitude(PolarAngle.fromCompassDegrees(9.0), 20.0)),
        Map(bd_2_0 -> AngleAndAltitude(PolarAngle.fromCompassDegrees(9.0), 24.0))
      )

      val meanTrajectory = ExtractionAndEstimation.meanTrajectory(trajectories)

      meanTrajectory.size mustEqual 2

      meanTrajectory(bd_1_5).angle.asCompassDegrees must beCloseTo(2.5 within significantFigures)
      meanTrajectory(bd_1_5).angleStdDevDegrees must beCloseTo(0.707107 within significantFigures)
      meanTrajectory(bd_1_5).altitudeMeters must beCloseTo(12.0 within significantFigures)
      meanTrajectory(bd_1_5).altitudeStdDevMeters must beCloseTo(0.0 within significantFigures)
      meanTrajectory(bd_1_5).sourceCount mustEqual 2

      meanTrajectory(bd_2_0).angle.asCompassDegrees must beCloseTo(6.0 within significantFigures)
      meanTrajectory(bd_2_0).angleStdDevDegrees must beCloseTo(3.0 within significantFigures)
      meanTrajectory(bd_2_0).altitudeMeters must beCloseTo(18.0 within significantFigures)
      meanTrajectory(bd_2_0).altitudeStdDevMeters must beCloseTo(5.291503 within significantFigures)
      meanTrajectory(bd_2_0).sourceCount mustEqual 3
    }
  }
}