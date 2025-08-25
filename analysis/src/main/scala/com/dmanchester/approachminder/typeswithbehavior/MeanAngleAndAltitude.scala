package com.dmanchester.approachminder.typeswithbehavior

import com.dmanchester.approachminder.typeswithoutbehavior.AngleAndAltitude

/**
 * Mean angle and altitude values calculated from a series of positions. Includes standard deviation for each value, as
 * well as the count of positions included in the calculations.
 */
case class MeanAngleAndAltitude(angle: PolarAngle, angleStdDevInDegrees: Double, altitudeInMeters: Double, altitudeStdDevInMeters: Double, positionsCount: Int) {

  def calculateDeviation(position: AngleAndAltitude): DeviationFromMean = {

      val angleDevInDegrees = position.angle.minusAsDegrees(angle)
      val angleStdDevs = angleDevInDegrees / angleStdDevInDegrees
      val altitudeDevInMeters = position.altitudeInMeters - altitudeInMeters
      val altitudeStdDevs = altitudeDevInMeters / altitudeStdDevInMeters

    DeviationFromMean(angleDevInDegrees, angleStdDevs, altitudeDevInMeters, altitudeStdDevs)
  }
}