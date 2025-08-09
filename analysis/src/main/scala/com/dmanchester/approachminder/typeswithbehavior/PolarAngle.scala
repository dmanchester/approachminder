package com.dmanchester.approachminder.typeswithbehavior

import scala.math.Pi

/**
 * A polar angle.
 *
 * Named "PolarAngle" as opposed to "Angle" in part to avoid collision with JTS "Angle" type.
 */
case class PolarAngle private(asCompassDegrees: Double, asRadians: Double) {

  /**
   * Subtract another/"that" angle from this one.
   *
   * @param that The other angle.
   * @return The difference between the angles. -- If the shortest arc from this angle to "that" runs counterclockwise,
   *         the result is positive. If the shortest arc runs clockwise, the result is negative. -- For example,
   *         PolarAngle.asCompassDegrees(135.0).minus(PolarAngle.asCompassDegrees(90.0)) is 45.0.
   */
  def minusAsDegrees(that: PolarAngle): Double = {

    val degreesNonNormalized = asCompassDegrees - that.asCompassDegrees

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

    val radiansNormalized = degreesNormalized match {
      case x if 0.0 <= x && x < 270.0        => -Pi / 180.0 * x + 0.5 * Pi
      case x  /* 270.0 <= x && x < 360.0 */  => -Pi / 180.0 * x + 2.5 * Pi
    }

    new PolarAngle(degreesNormalized, radiansNormalized)
  }

  def fromRadians(radians: Double): PolarAngle = {

    val radiansNormalized = radians % (2.0*Pi)  // in range (-2*pi, 2*pi)

    // The function that maps radians to compass degrees is discontinuous. Its component segments  have the same
    // slope (-180/pi). They differ only in their y-intercept.
    val yIntercept = radiansNormalized match {
      case x if -1.5*Pi < x && x <= 0.5*Pi =>  90.0
      case x if x > 0.5*Pi =>                 450.0
      case x if x <= -1.5*Pi =>              -270.0
    }

    val degreesNormalized = -180.0/Pi * radiansNormalized + yIntercept

    new PolarAngle(degreesNormalized, radiansNormalized)
  }
}
