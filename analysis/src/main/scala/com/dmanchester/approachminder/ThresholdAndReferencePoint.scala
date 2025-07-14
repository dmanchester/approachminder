package com.dmanchester.approachminder

import com.dmanchester.approachminder.typeswithoutbehavior.HasLongLat

class ThresholdAndReferencePoint private(val threshold: Airport#RunwaySurface#Runway, val referencePoint: HasLongLat)

object ThresholdAndReferencePoint {
  def apply(threshold: Airport#RunwaySurface#Runway, referencePoint: HasLongLat): ThresholdAndReferencePoint = new ThresholdAndReferencePoint(threshold, referencePoint)
}