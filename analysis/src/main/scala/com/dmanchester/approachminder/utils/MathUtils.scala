package com.dmanchester.approachminder.utils

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
}
