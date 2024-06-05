package com.dmanchester.approachminder

class DeviationFromApproach private(val threshold: Airport#RunwaySurface#RunwayThreshold, val thresholdDistanceMeters: Double, val verticalDevMeters: Double, val horizontalDevMeters: Double, val normalizedEuclideanDistance: Double)

object DeviationFromApproach {
  def apply(threshold: Airport#RunwaySurface#RunwayThreshold, thresholdDistanceMeters: Double, verticalDevMeters: Double, horizontalDevMeters: Double, normalizedEuclideanDistance: Double): DeviationFromApproach = new DeviationFromApproach(threshold, thresholdDistanceMeters, verticalDevMeters, horizontalDevMeters, normalizedEuclideanDistance)
}
