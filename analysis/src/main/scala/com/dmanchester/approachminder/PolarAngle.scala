package com.dmanchester.approachminder

import scala.math.Pi

class PolarAngle private(val toCompassDegrees: Double, val toRadians: Double) {

  /**
   * Subtract another/"that" polar angle from this one.
   *
   * If the shortest arc from "that" angle to this one runs clockwise, the result is positive.
   *
   * If the shortest arc runs counterclockwise, the result is negative.
   *
   * For example, PolarAngle.fromCompassDegrees(135.0).minus(PolarAngle.fromCompassDegrees(90.0))
   * is 45.0.
   *
   * @param thatAngle
   * @return
   */
  def minusAsDegrees(thatAngle: PolarAngle): Double = {

    val degreesNonNormalized = toCompassDegrees - thatAngle.toCompassDegrees

    degreesNonNormalized match {
      case x if x <= -180.0 => x + 360.0
      case x if x > 180.0   => x - 360.0
      case x                => x
    }
  }
}

object PolarAngle {

  def fromCompassDegrees(degrees: Double): PolarAngle = {

    val degreesPartiallyNormalized = degrees % 360.0  // in range (-360, 360)

    val degreesNormalized = if (degreesPartiallyNormalized >= 0.0) {
      degreesPartiallyNormalized
    } else {
      degreesPartiallyNormalized + 360.0
    }  // in range [0, 360)

    // Could potentially replace the above with "val degreesNormalized = (degrees - 180.0) % 180.0 + 180.0", but haven't tested.

    val radiansNormalized = degreesNormalized match {
      case x if 0.0 <= x && x < 270.0 => -Pi / 180.0 * x + 0.5 * Pi
      case x if 270.0 <= x && x < 360.0 => -Pi / 180.0 * x + 2.5 * Pi
    }

    new PolarAngle(degreesNormalized, radiansNormalized)
  }

  def fromRadians(radians: Double): PolarAngle = {

    val radiansNormalized = radians % (2.0*Pi)  // in range (-2*pi, 2*pi)

    // The function that maps radians to compass degrees is discontinuous. Its component segments
    // have the same slope (-180/pi). They differ only in their y-intercept.
    val yIntercept = radiansNormalized match {
      case x if -1.5*Pi < x && x <= 0.5*Pi =>  90.0
      case x if x > 0.5*Pi =>                 450.0
      case x if x <= -1.5*Pi =>              -270.0
    }

    val degreesNormalized = -180.0/Pi * radiansNormalized + yIntercept

    new PolarAngle(degreesNormalized, radiansNormalized)
  }
}
