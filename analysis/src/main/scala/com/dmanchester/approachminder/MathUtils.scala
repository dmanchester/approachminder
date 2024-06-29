package com.dmanchester.approachminder

import scala.math.{pow, sin, sqrt, toRadians}

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

  /**
   * Calculate the length of an Isosceles triangle's base.
   *
   * @param apexAngleDegrees the triangle's apex angle, in degrees
   * @param legLength the length of the triangle's legs
   * @return The length. If the angle is negative, the length is, too.
   */
  def isoscelesBaseLength(apexAngleDegrees: Double, legLength: Double): Double = {

    val apexAngleRadians = toRadians(apexAngleDegrees)
    2.0 * legLength * sin(apexAngleRadians / 2.0)
  }

  def hypotenuseLength(aLength: Double, bLength: Double): Double = {
    sqrt(pow(aLength, 2) + pow(bLength, 2))
  }
}
