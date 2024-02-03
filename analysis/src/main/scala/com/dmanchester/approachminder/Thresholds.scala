package com.dmanchester.approachminder

import scala.annotation.tailrec

class Thresholds private(val theThresholds: Iterable[Airport#RunwaySurface#RunwayThreshold]) {

  // TODO Need test coverage
  def findThresholdCrossedInboundAndInterpolatePoint(flightSegment: (HasLongLat, HasLongLat)): Option[(Airport#RunwaySurface#RunwayThreshold, HasLongLat, Double)] = {
    doFindThresholdCrossedInboundAndInterpolatePoint(theThresholds.iterator, flightSegment)
  }

  @tailrec private def doFindThresholdCrossedInboundAndInterpolatePoint(thresholdsIterator: Iterator[Airport#RunwaySurface#RunwayThreshold], flightSegment: (HasLongLat, HasLongLat)): Option[(Airport#RunwaySurface#RunwayThreshold, HasLongLat, Double)] = {

    if (!thresholdsIterator.hasNext) {
      None
    } else {
      val threshold = thresholdsIterator.next()
      val inboundCrossingPoint = threshold.interpolateInboundCrossingPoint(flightSegment)

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
  def apply(theThresholds: Iterable[Airport#RunwaySurface#RunwayThreshold]): Thresholds = new Thresholds(theThresholds)
}