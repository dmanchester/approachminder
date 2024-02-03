package com.dmanchester.approachminder

import org.apache.commons.math3.stat.StatUtils

import scala.math.sqrt


// TODO Should this be AngleAndAltitudeDistribution? Signature of ExtractionAndEstimation.meanTrajectory would be more coherent

class PositionDistribution private(val angleMean: PolarAngle, val angleStdDevDegrees: Double, val altitudeMeanMeters: Double, val altitudeStdDevMeters: Double, val valuesIncluded: Int) {
  override def toString = s"${this.getClass.getSimpleName}($angleMean,$angleStdDevDegrees,$altitudeMeanMeters,$altitudeStdDevMeters,$valuesIncluded)"

  def deviation(position: AngleAndAltitude): DeviationFromPositionDistribution = {

    val angleDevDegrees = position.angle.minusAsDegrees(angleMean)
    val angleStdDevs = angleDevDegrees / angleStdDevDegrees
    val altitudeDevMeters = position.altitudeMeters - altitudeMeanMeters
    val altitudeStdDevs = altitudeDevMeters / altitudeStdDevMeters

    DeviationFromPositionDistribution(angleDevDegrees, angleStdDevs, altitudeDevMeters, altitudeStdDevs)
  }
}

object PositionDistribution {
  def fromDataOption(positions: Iterable[AngleAndAltitude]): Option[PositionDistribution] = {

    Option.when(positions.size >= 2) {

      val angles = positions.map(_.angle)
      val (angleMean, angleStdDevDegrees) = PolarAngles.circularMeanAndStdDevDegrees(angles)

      val altitudesMetersAsArray = positions.map(_.altitudeMeters).toArray
      val altitudeMeanMeters = StatUtils.mean(altitudesMetersAsArray)
      val altitudeStdDevMeters = sqrt(StatUtils.variance(altitudesMetersAsArray, altitudeMeanMeters))

      new PositionDistribution(angleMean, angleStdDevDegrees, altitudeMeanMeters, altitudeStdDevMeters, positions.size)
    }
  }
}
