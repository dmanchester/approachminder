package com.dmanchester.approachminder

class ThresholdAndReferencePoint private(val threshold: Airport#RunwaySurface#RunwayThreshold, val referencePoint: HasLongLat)

object ThresholdAndReferencePoint {
  def apply(threshold: Airport#RunwaySurface#RunwayThreshold, referencePoint: HasLongLat): ThresholdAndReferencePoint = new ThresholdAndReferencePoint(threshold, referencePoint)
}