package com.dmanchester.approachminder.utils

import com.dmanchester.approachminder.PolarAngles
import com.dmanchester.approachminder.typeswithbehavior.MeanAngleAndAltitude
import com.dmanchester.approachminder.typeswithoutbehavior.AngleAndAltitude
import org.apache.commons.math3.stat.StatUtils

import scala.math.sqrt

object MathUtils {

  /**
   * Interpolate the value that is some percentage of the distance from one number to another.
   *
   * @param a The first number.
   * @param b The second number.
   * @param percentage The percentage.
   * @return The interpolated value.
   */
  def interpolateScalar(a: Double, b: Double, percentage: Double): Double = a + percentage * (b - a)

  /**
   * Round a value down to the nearest multiple of a step size.
   *
   * If the value is a multiple of the step size, returns it unchanged.
   *
   * This function's behavior is undefined for negative values and negative step sizes.
   *
   * @param value The value.
   * @param stepSize The step size.
   * @return The result.
   */
  def roundDownToNearestMultiple(value: BigDecimal, stepSize: BigDecimal): BigDecimal = {
    val (divisionIntegralValue, _) = value /% stepSize
    stepSize * divisionIntegralValue
  }

  /**
   * Divide two Doubles. Provides alternate handling of the 0.0 / 0.0 case, returning 0.0 (instead of NaN).
   *
   * @param numerator The numerator.
   * @param denominator The denominator.
   * @return The quotient.
   */
  def divideWithAlt0_0Handling(numerator: Double, denominator: Double): Double = {
    numerator match {
      case 0.0 => 0.0
      case _ => numerator / denominator
    }
  }

  /**
   * From a series of two or more positions, calculate the mean angle and altitude values. Includes standard deviation
   * for each value.
   *
   * @param positions The positions.
   * @return The means and standard deviations, as well as the count of positions included in the calculations, packaged
   *         in Some. -- Or, None if less than two positions were provided.
   */
  def calculateMeanAngleAndAltitude(positions: Iterable[AngleAndAltitude]): Option[MeanAngleAndAltitude] = {

    Option.when(positions.size >= 2) {

      val angles = positions.map(_.angle)
      val (meanAngle, angleStdDevInDegrees) = PolarAngles.circularMeanAndStdDevDegrees(angles)

      val altitudesMetersAsArray = positions.map(_.altitudeInMeters).toArray
      val meanAltitudeInMeters = StatUtils.mean(altitudesMetersAsArray)
      val altitudeStdDevInMeters = sqrt(StatUtils.variance(altitudesMetersAsArray, meanAltitudeInMeters))

      MeanAngleAndAltitude(meanAngle, angleStdDevInDegrees, meanAltitudeInMeters, altitudeStdDevInMeters, positions.size)
    }
  }
}
