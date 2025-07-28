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
}
