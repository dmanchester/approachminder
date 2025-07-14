package com.dmanchester.approachminder

import com.dmanchester.approachminder.typeswithbehavior.Airport

class ApproachSegmentWithDeviation private(val threshold: Airport#RunwaySurface#Runway, val thresholdDistanceMeters: Double, val verticalDevMeters: Double, val horizontalDevMeters: Double, val normalizedEuclideanDistance: Double)

object ApproachSegmentWithDeviation {
  def apply(threshold: Airport#RunwaySurface#Runway, thresholdDistanceMeters: Double, verticalDevMeters: Double, horizontalDevMeters: Double, normalizedEuclideanDistance: Double): ApproachSegmentWithDeviation = new ApproachSegmentWithDeviation(threshold, thresholdDistanceMeters, verticalDevMeters, horizontalDevMeters, normalizedEuclideanDistance)
}
