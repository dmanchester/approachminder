package com.dmanchester.approachminder

import com.dmanchester.approachminder.typeswithbehavior.Airport
import com.dmanchester.approachminder.typeswithoutbehavior.HasLongLat

import scala.annotation.tailrec

class Runways private(val theRunways: Iterable[Airport#RunwaySurface#Runway]) {

  // TODO Need test coverage
  def findThresholdCrossedInboundAndInterpolatePoint(flightSegment: (HasLongLat, HasLongLat)): Option[(Airport#RunwaySurface#Runway, HasLongLat, Double)] = {
    doFindThresholdCrossedInboundAndInterpolatePoint(theRunways.iterator, flightSegment)
  }

  @tailrec private def doFindThresholdCrossedInboundAndInterpolatePoint(runwaysIterator: Iterator[Airport#RunwaySurface#Runway], flightSegment: (HasLongLat, HasLongLat)): Option[(Airport#RunwaySurface#Runway, HasLongLat, Double)] = {

    if (!runwaysIterator.hasNext) {
      None
    } else {
      val runway = runwaysIterator.next()
      val inboundCrossingPoint = runway.testForInboundThresholdCrossing(flightSegment)

      if (inboundCrossingPoint.isDefined) {
        inboundCrossingPoint.map { case (point, percentageFromSegStartToSegEnd) =>
          (runway, point, percentageFromSegStartToSegEnd)
        }
      } else {
        doFindThresholdCrossedInboundAndInterpolatePoint(runwaysIterator, flightSegment)
      }
    }
  }
}

object Runways {
  def apply(theRunways: Iterable[Airport#RunwaySurface#Runway]): Runways = new Runways(theRunways)
}