package com.dmanchester.approachminder.utils

import com.dmanchester.approachminder.typeswithbehavior.PolarAngle
import org.geotools.measure.Units

import scala.math.{atan2, cos, pow, sin, sqrt, toRadians}

object MathUtils {

  /**
   * Convert feet to meters.
   *
   * @param feet A value in feet.
   * @return The value in meters.
   */
  def feetToMeters(feet: Double): Double = 0.3048 * feet

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
   * Calculate the length of a right triangle's hypotenuse.
   *
   * @param legALength The length of one leg.
   * @param legBLength The length of the other leg.
   * @return The length of the hypotenuse.
   */
  def hypotenuseLength(legALength: Double, legBLength: Double): Double = sqrt(pow(legALength, 2) + pow(legBLength, 2))

  /**
   * Calculate the length of an Isosceles triangle's base.
   *
   * @param apexAngleDegrees The triangle's apex angle, in degrees.
   * @param legLength The length of the triangle's legs.
   * @return The length. If the angle is negative, the length is, too. -- TODO Push the responsibility of maintaining
   *         negativity to calling code?
   */
  def isoscelesBaseLength(apexAngleDegrees: Double, legLength: Double): Double = {

    val apexAngleRadians = toRadians(apexAngleDegrees)
    2.0 * legLength * sin(apexAngleRadians / 2.0)
  }

  private def circularMean(angles: Iterable[PolarAngle]): PolarAngle = {

    val anglesRadians = angles.map(_.asRadians)
    val sineTerm = anglesRadians.map(sin).sum
    val cosineTerm = anglesRadians.map(cos).sum

    val meanRadians = atan2(sineTerm, cosineTerm)
    PolarAngle.fromRadians(meanRadians)
  }

  /**
   * Calculate the circular mean and standard deviation of a collection of angles.
   *
   * For more information on the circular mean, see https://en.wikipedia.org/wiki/Circular_mean.
   *
   * At least two angles are required.
   *
   * @param angles The angles.
   * @throws java.lang.IllegalArgumentException If less than two angles are provided.
   * @return The circular mean and standard deviation.
   */
  @throws(classOf[IllegalArgumentException])
  def circularMeanAndStdDevDegrees(angles: Iterable[PolarAngle]): (PolarAngle, Double) = {

    if (angles.size < 2) {
      throw new IllegalArgumentException(s"Need at least 2 angles; got ${angles.size}!")
    }

    val thisCircularMean = MathUtils.circularMean(angles)

    // Complementing "circular mean", there is also "circular standard deviation":
    // https://en.wikipedia.org/wiki/Directional_statistics#Standard_deviation
    // However, it is not a particularly intuitive measure, and the appropriateness of simply
    // swapping it in for traditional standard deviation--for example, in normalized Euclidean
    // distance--is unclear.
    //
    // Thus, we rely on traditional standard deviation.
    //
    // That it is acceptable to use traditional standard deviation with polar angles seems to be
    // borne out by, for example, K.A. Verrall and R.L. Williams' 1982 paper "A Method for
    // Estimating the Standard Deviation of Wind Directions".
    //
    // That paper happens to propose a polar-angle alternative to traditional standard deviation,
    // but only because of the difficulties of calculating it on a continuous basis from streamed
    // readings when storage space is limited.

    val squaredDifferencesFromMeanDegrees = angles.map { angle =>
      val differenceFromMeanDegrees = angle.minusAsDegrees(thisCircularMean)
      pow(differenceFromMeanDegrees, 2)
    }

    val stdDevDegrees = sqrt(squaredDifferencesFromMeanDegrees.sum / (angles.size - 1))

    (thisCircularMean, stdDevDegrees)
  }
}
