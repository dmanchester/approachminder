package com.dmanchester.approachminder.typeswithoutbehavior

import com.dmanchester.approachminder.typeswithbehavior.Airport

case class RunwayAndReferencePoint(runway: Airport#RunwaySurface#Runway, referencePoint: HasLongLat)
