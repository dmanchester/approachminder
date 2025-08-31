package com.dmanchester.approachminder.typeswithbehavior

import com.dmanchester.approachminder.typeswithoutbehavior.AngleAndAltitude
import com.dmanchester.approachminder.utils.MathUtils.divideWithAlt0_0Handling

/**
 * Mean angle and altitude values calculated from a series of positions. Includes standard deviation for each value, as
 * well as the count of positions included in the calculations.
 */
case class MeanAngleAndAltitude(angle: PolarAngle, angleStdDevInDegrees: Double, altitudeInMeters: Double, altitudeStdDevInMeters: Double, positionsCount: Int) {

  /**
   * Calculate a position's deviation from the mean angle and altitude values.
   *
   * @param position The position.
   * @return The deviation. If the mean's standard deviation for angle or altitude is zero, the corresponding "stdDevs"
   *         value in the deviation is: infinity, if the position's deviation is non-zero; or zero, if the position's
   *         deviation is zero.
   */
  def calculateDeviation(position: AngleAndAltitude): DeviationFromMean = {

      val angleDevInDegrees = position.angle.minusAsDegrees(angle)
      val angleStdDevs = divideWithAlt0_0Handling(angleDevInDegrees, angleStdDevInDegrees)
      val altitudeDevInMeters = position.altitudeInMeters - altitudeInMeters
      val altitudeStdDevs = divideWithAlt0_0Handling(altitudeDevInMeters, altitudeStdDevInMeters)

    DeviationFromMean(angleDevInDegrees, angleStdDevs, altitudeDevInMeters, altitudeStdDevs)
  }
}