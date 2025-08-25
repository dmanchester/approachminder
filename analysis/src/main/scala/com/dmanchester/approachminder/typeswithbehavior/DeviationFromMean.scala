package com.dmanchester.approachminder.typeswithbehavior

import scala.math.{pow, sqrt}

/**
 * Deviation from a mean angle and altitude.
 *
 * Also includes the normalized Euclidean distance (the square root of the squared standard deviations).
 */
case class DeviationFromMean(angleDevInDegrees: Double, angleStdDevs: Double, altitudeDevInMeters: Double, altitudeStdDevs: Double) {
  val normalizedEuclideanDistance = sqrt(pow(angleStdDevs, 2) + pow(altitudeStdDevs, 2))
}