package com.dmanchester.approachminder

import com.dmanchester.approachminder.Airports.sfo
import com.dmanchester.approachminder.SharedResources.*
import com.dmanchester.approachminder.typeswithoutbehavior.LongLat
import org.specs2.mutable.*

class AirportSpec extends Specification {

  private val sfoRunway28L = sfo.runwayByName("28L").get

  "constructors/'apply' pseudo-constructors of Airport/RunwaySurface/Runway" should {
    "process the RunwaySurfaceTemplates in order and correctly assign runway thresholds' left and right points" in {

      sfo.runways(6).name mustEqual "10R"
      sfo.runways(6).thresholdLeft must beCloseInTwoDimensionsTo(sfoThresholdLeft10R, significantFigures)
      sfo.runways(6).thresholdRight must beCloseInTwoDimensionsTo(sfoThresholdRight10R, significantFigures)

      sfo.runways(7).name mustEqual "28L"
      sfo.runways(7).thresholdLeft must beCloseInTwoDimensionsTo(sfoThresholdLeft28L, significantFigures)
      sfo.runways(7).thresholdRight must beCloseInTwoDimensionsTo(sfoThresholdRight28L, significantFigures)
    }
  }

  "Airport.runwayByName" should {
    "find a runway that exists" in {
      sfo.runwayByName("10R") must beSome
    }

    "handle a runway that doesn't exist" in {
      sfo.runwayByName("999") must beNone
    }
  }

  "RunwaySurface.contains" should {
    "confirm a point is on the runway surface" in {
      sfoRunway28L.surface.contains(sfoPointB) must beTrue
    }

    "confirm a point is not on the runway surface" in {
      sfoRunway28L.surface.contains(sfoPointA) must beFalse
    }
  }

  "Runway.testForInboundThresholdCrossing" should {

    "handle a flight segment that crosses inbound" in {

      val flightSegment = (sfoPointA, sfoPointB)
      val inboundCrossingPoint = sfoRunway28L.testForInboundThresholdCrossing(flightSegment)
      inboundCrossingPoint must beSome

      val point = inboundCrossingPoint.get._1
      val percentageFromSegStartToSegEnd = inboundCrossingPoint.get._2

      point must beCloseInTwoDimensionsTo(sfoPointF, significantFigures)
      percentageFromSegStartToSegEnd must beCloseTo(0.347885 within significantFigures)  // See GeographicCalculatorSpec for source of this value.
    }

    "handle a flight segment that crosses outbound" in {
      val flightSegment = (sfoPointB, sfoPointA)
      sfoRunway28L.testForInboundThresholdCrossing(flightSegment) must beNone
    }

    "consider as 'not crossing' a flight segment entirely within the boundaries of the runway surface" in {
      val flightSegment = (sfoPointB, sfoPointE)
      sfoRunway28L.testForInboundThresholdCrossing(flightSegment) must beNone
    }

    "consider as 'not crossing' a flight segment that crosses inbound but ends outside the runway surface (a real-world case would be, aircraft clips corner of runway surface at altitude)" in {
      val flightSegment = (sfoPointA, sfoPointD)
      sfoRunway28L.testForInboundThresholdCrossing(flightSegment) must beNone
    }
  }

  "Runway.pointOnRunwayCenterline" should {

    "return the appropriate point" in {
      val point = sfoRunway28L.pointOnRunwayCenterline(0.25)
      // Confirmed the following point's correctness visually, with online map.
      point must beCloseInTwoDimensionsTo(LongLat(-122.367037, 37.615358), significantFigures)
    }
  }
}
