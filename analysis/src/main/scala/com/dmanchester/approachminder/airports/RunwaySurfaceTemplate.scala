package com.dmanchester.approachminder.airports

import com.dmanchester.approachminder.geo.HasLongLat

case class RunwaySurfaceTemplate(widthInMeters: Double, runway0Name: String, runway0ThresholdCenter: HasLongLat, runway1Name: String, runway1ThresholdCenter: HasLongLat)
