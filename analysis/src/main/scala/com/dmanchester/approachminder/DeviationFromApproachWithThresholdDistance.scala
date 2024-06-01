package com.dmanchester.approachminder

class DeviationFromApproachWithThresholdDistance private(val threshold: Airport#RunwaySurface#RunwayThreshold, val thresholdDistanceMeters: Double, val verticalDevMeters: Double, val horizontalDevMeters: Double, val normalizedEuclideanDistance: Double)

object DeviationFromApproachWithThresholdDistance {
  def apply(threshold: Airport#RunwaySurface#RunwayThreshold, thresholdDistanceMeters: Double, verticalDevMeters: Double, horizontalDevMeters: Double, normalizedEuclideanDistance: Double): DeviationFromApproachWithThresholdDistance = new DeviationFromApproachWithThresholdDistance(threshold, thresholdDistanceMeters, verticalDevMeters, horizontalDevMeters, normalizedEuclideanDistance)
}
