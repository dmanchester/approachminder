package com.dmanchester.approachminder

import com.dmanchester.approachminder.typeswithoutbehavior.HasLongLat

import scala.annotation.tailrec

class Thresholds private(val theThresholds: Iterable[Airport#RunwaySurface#Runway]) {

  // TODO Need test coverage
  def findThresholdCrossedInboundAndInterpolatePoint(flightSegment: (HasLongLat, HasLongLat)): Option[(Airport#RunwaySurface#Runway, HasLongLat, Double)] = {
    doFindThresholdCrossedInboundAndInterpolatePoint(theThresholds.iterator, flightSegment)
  }

  @tailrec private def doFindThresholdCrossedInboundAndInterpolatePoint(thresholdsIterator: Iterator[Airport#RunwaySurface#Runway], flightSegment: (HasLongLat, HasLongLat)): Option[(Airport#RunwaySurface#Runway, HasLongLat, Double)] = {

    if (!thresholdsIterator.hasNext) {
      None
    } else {
      val threshold = thresholdsIterator.next()
      val inboundCrossingPoint = threshold.testForInboundThresholdCrossing(flightSegment)

      if (inboundCrossingPoint.isDefined) {
        inboundCrossingPoint.map { case (point, percentageFromSegStartToSegEnd) =>
          (threshold, point, percentageFromSegStartToSegEnd)
        }
      } else {
        doFindThresholdCrossedInboundAndInterpolatePoint(thresholdsIterator, flightSegment)
      }
    }
  }
}

object Thresholds {
  def apply(theThresholds: Iterable[Airport#RunwaySurface#Runway]): Thresholds = new Thresholds(theThresholds)
}