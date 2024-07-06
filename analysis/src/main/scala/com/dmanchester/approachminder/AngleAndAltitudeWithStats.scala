package com.dmanchester.approachminder

import org.apache.commons.math3.stat.StatUtils

import scala.math.sqrt

class AngleAndAltitudeWithStats private(val angle: PolarAngle, val angleStdDevDegrees: Double, val altitudeMeters: Double, val altitudeStdDevMeters: Double, val sourceCount: Int) {
  override def toString = s"${this.getClass.getSimpleName}($angle,$angleStdDevDegrees,$altitudeMeters,$altitudeStdDevMeters,$sourceCount)"

  def deviation(position: AngleAndAltitude): AngleAndAltitudeDeviation = {

    val angleDevDegrees = position.angle.minusAsDegrees(angle)
    val angleStdDevs = angleDevDegrees / angleStdDevDegrees
    val altitudeDevMeters = position.altitudeMeters - altitudeMeters
    val altitudeStdDevs = altitudeDevMeters / altitudeStdDevMeters

    AngleAndAltitudeDeviation(angleDevDegrees, angleStdDevs, altitudeDevMeters, altitudeStdDevs)
  }
}

object AngleAndAltitudeWithStats {
  def fromDataOption(positions: Iterable[AngleAndAltitude]): Option[AngleAndAltitudeWithStats] = {

    Option.when(positions.size >= 2) {  // TODO If "positions" were a type that enforced this invariant, could move to a non-Option "fromData".

      val angles = positions.map(_.angle)
      val (angleMean, angleStdDevDegrees) = PolarAngles.circularMeanAndStdDevDegrees(angles)

      val altitudesMetersAsArray = positions.map(_.altitudeMeters).toArray
      val altitudeMeanMeters = StatUtils.mean(altitudesMetersAsArray)
      val altitudeStdDevMeters = sqrt(StatUtils.variance(altitudesMetersAsArray, altitudeMeanMeters))

      new AngleAndAltitudeWithStats(angleMean, angleStdDevDegrees, altitudeMeanMeters, altitudeStdDevMeters, positions.size)
    }
  }
}
