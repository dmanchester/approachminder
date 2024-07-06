package com.dmanchester.approachminder

import scala.math.{pow, sqrt}

class AngleAndAltitudeDeviation private(val angleDevDegrees: Double, val angleStdDevs: Double, val altitudeDevMeters: Double, val altitudeStdDevs: Double, val normalizedEuclideanDistance: Double) {
  override def toString = s"${this.getClass.getSimpleName}($angleDevDegrees,$angleStdDevs,$altitudeDevMeters,$altitudeStdDevs,$normalizedEuclideanDistance)"

}

object AngleAndAltitudeDeviation {

  def apply(angleDevDegrees: Double, angleStdDevs: Double, altitudeDevMeters: Double, altitudeStdDevs: Double): AngleAndAltitudeDeviation = {
    val normalizedEuclideanDistance = sqrt(pow(angleStdDevs, 2) + pow(altitudeStdDevs, 2))
    new AngleAndAltitudeDeviation(angleDevDegrees, angleStdDevs, altitudeDevMeters, altitudeStdDevs, normalizedEuclideanDistance)
  }
}