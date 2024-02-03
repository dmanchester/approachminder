package com.dmanchester.approachminder

import Airports.sfo
import SharedResources.{beCloseInTwoDimensionsTo, sfoPointA, sfoPointB, sfoPointD, sfoPointE, sfoPointF, sfoThresholdLeft10R, sfoThresholdLeft28L, sfoThresholdRight10R, sfoThresholdRight28L, significantFigures}
import org.specs2.mutable._

class AirportSpec extends Specification {

  "constructors/'apply' pseudo-constructors of Airport/RunwaySurface/RunwayThreshold" should {
    "process the RunwaySurfaceTemplates in order and correctly assign thresholds' left and right points" in {

      sfo.thresholds(6).name mustEqual("10R")
      sfo.thresholds(6).left must beCloseInTwoDimensionsTo(sfoThresholdLeft10R, significantFigures)
      sfo.thresholds(6).right must beCloseInTwoDimensionsTo(sfoThresholdRight10R, significantFigures)

      sfo.thresholds(7).name mustEqual("28L")
      sfo.thresholds(7).left must beCloseInTwoDimensionsTo(sfoThresholdLeft28L, significantFigures)
      sfo.thresholds(7).right must beCloseInTwoDimensionsTo(sfoThresholdRight28L, significantFigures)
    }
  }

  "Airport.thresholdByName" should {
    "find a runway threshold that exists" in {
      sfo.thresholdByName("10R") must beSome
    }

    "handle a runway threshold that doesn't exist" in {
      sfo.thresholdByName("999") must beNone
    }
  }

  "RunwaySurface.contains" should {
    "confirm a point is on the runway surface" in {
      sfo.thresholdByName("28L").get.runwaySurface.contains(sfoPointB) must beTrue
    }

    "confirm a point is not on the runway surface" in {
      sfo.thresholdByName("28L").get.runwaySurface.contains(sfoPointA) must beFalse
    }
  }

  "RunwayThreshold.interpolateInboundCrossingPoint" should {

    val sfoThreshold28L = sfo.thresholdByName("28L").get

    "handle a flight segment that crosses inbound" in {

      val flightSegment = (sfoPointA, sfoPointB)
      val inboundCrossingPoint = sfoThreshold28L.interpolateInboundCrossingPoint(flightSegment)
      inboundCrossingPoint must beSome

      val point = inboundCrossingPoint.get._1
      val percentageFromSegStartToSegEnd = inboundCrossingPoint.get._2

      point must beCloseInTwoDimensionsTo(sfoPointF, significantFigures)
      percentageFromSegStartToSegEnd must beCloseTo(0.347885 within significantFigures)  // see GeographicCalculatorSpec for source of this value
    }

    "handle a flight segment that crosses outbound" in {
      val flightSegment = (sfoPointB, sfoPointA)
      sfoThreshold28L.interpolateInboundCrossingPoint(flightSegment) must beNone
    }

    "consider as 'not crossing' a flight segment entirely within the boundaries of the runway surface" in {
      val flightSegment = (sfoPointB, sfoPointE)
      sfoThreshold28L.interpolateInboundCrossingPoint(flightSegment) must beNone
    }

    "consider as 'not crossing' a flight segment that crosses inbound but ends outside the runway surface (a real-world case would be, aircraft clips corner of runway surface at altitude)" in {
      val flightSegment = (sfoPointA, sfoPointD)
      sfoThreshold28L.interpolateInboundCrossingPoint(flightSegment) must beNone
    }
  }
}
