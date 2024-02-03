package com.dmanchester.approachminder

import scala.math.Pi

object MathUtils {

  /**
   * Whether `x` is between `a` and `b`, inclusive. Handles `a` <= `b` and `b` <= `a`, as well as
   * positive/negative/mixed `a` and `b`.
   *
   * @param x
   * @param a
   * @param b
   * @return
   */
  def isBetween(x: Double, a: Double, b: Double): Boolean = {

    (a <= x && x <= b) || (b <= x && x <= a)
  }

  /**
   * Interpolate the scalar value that is `percentage` of the distance from `a` to `b`.
   *
   * @param a
   * @param b
   * @param percentage
   * @return
   */
  def interpolateScalar(a: Double, b: Double, percentage: Double): Double = a + percentage * (b - a)
}
