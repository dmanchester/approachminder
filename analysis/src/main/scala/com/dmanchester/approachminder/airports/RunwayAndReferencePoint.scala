package com.dmanchester.approachminder.airports

import com.dmanchester.approachminder.geo.HasLongLat

case class RunwayAndReferencePoint(runway: Airport#RunwaySurface#Runway, referencePoint: HasLongLat)
