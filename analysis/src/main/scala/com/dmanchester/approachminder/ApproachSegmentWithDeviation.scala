package com.dmanchester.approachminder

class ApproachSegmentWithDeviation private(val threshold: Airport#RunwaySurface#RunwayThreshold, val thresholdDistanceMeters: Double, val verticalDevMeters: Double, val horizontalDevMeters: Double, val normalizedEuclideanDistance: Double)

object ApproachSegmentWithDeviation {
  def apply(threshold: Airport#RunwaySurface#RunwayThreshold, thresholdDistanceMeters: Double, verticalDevMeters: Double, horizontalDevMeters: Double, normalizedEuclideanDistance: Double): ApproachSegmentWithDeviation = new ApproachSegmentWithDeviation(threshold, thresholdDistanceMeters, verticalDevMeters, horizontalDevMeters, normalizedEuclideanDistance)
}
