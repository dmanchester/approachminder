package com.dmanchester.approachminder

import com.dmanchester.approachminder.typeswithbehavior.Airport

case class ApproachSegmentWithDeviation(runway: Airport#RunwaySurface#Runway, thresholdDistanceMeters: Double, verticalDevMeters: Double, horizontalDevMeters: Double, normalizedEuclideanDistance: Double)
