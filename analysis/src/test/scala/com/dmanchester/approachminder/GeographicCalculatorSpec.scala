package com.dmanchester.approachminder

import Airports.sfoData
import SharedResources._

import org.specs2.mutable._

class GeographicCalculatorSpec extends Specification {

  // Points G - J are laid out as follows; "X" is the reference point:
  //
  //       G
  //      /
  //     H
  //    /
  //   I
  //  /
  // J     X
  //
  // Their distances from X are as follows, rounded to the nearest tenth of a kilometer:
  //
  // Point  Distance (km)
  // -----  -------------
  //   G       166.5
  //   H       111.3
  //   I        58.2
  //   J        26.3
  //
  // At its closest point, line segment G-J is ~26.0 km from X.

  val referencePoint = LongLat(-122, 38)
  val pointG = LongLat(-122, 39.5)
  val pointH = LongLat(-122.1, 39)
  val pointI = LongLat(-122.2, 38.5)
  val pointJ = LongLat(-122.3, 38)

  "distanceInMeters" should {
    "calculate distance" in {
      val runway28L10RLengthInMeters = sfoCalculator.distanceInMeters(sfoData.thresholdCenter28L, sfoData.thresholdCenter10R)

      // APT_RWY.csv gives length as 11381 ft., which converts to 3469 meters.
      runway28L10RLengthInMeters must beCloseTo(3467.714078 within significantFigures)
    }
  }

  "angle" should {
    "calculate angles between 0 and 270 deg." in {  // one formula covers this range of outputs
      val angle = sfoCalculator.angle(referencePoint, LongLat(-121, 39))
      angle.toCompassDegrees must beCloseTo(37.226482 within significantFigures)
    }

    "calculate angles between 270 and 360 deg." in { // another formula covers this range of outputs
      val angle = sfoCalculator.angle(referencePoint, LongLat(-123, 39))
      angle.toCompassDegrees must beCloseTo(321.528328 within significantFigures)
    }
  }

  "intersection" should {

    val thresholdRunway28L = (sfoThresholdLeft28L, sfoThresholdRight28L)

    "calculate the point where two segments intersect" in {

      val intersection = sfoCalculator.intersection((sfoPointA, sfoPointB), thresholdRunway28L)
      intersection must beSome

      val point = intersection.get._1
      val percentageFromFlightSegStartToSegEnd = intersection.get._2

      point must beCloseInTwoDimensionsTo(sfoPointF, significantFigures)
      percentageFromFlightSegStartToSegEnd must beCloseTo(0.347885 within significantFigures)
      // Calculated distances as follows, using sfoCalculator.distanceInMeters():
      //
      //     * Point A to Point F': 31.08932 meters
      //     * Point A to Point B:  89.36675 meters
      //
      // (Point F', at LongLat(-122.35838656432684,37.611655553983894), is a refinement of Point F.
      // The extra decimal places are needed to calculate the distance to six significant digits.)
      //
      // 31.08932 / 89.36675 is approximately equal to 0.347885.
    }

    "confirm that two segments don't intersect" in {
      val crossingPoint = sfoCalculator.intersection((sfoPointA, sfoPointC), thresholdRunway28L)
      crossingPoint must beNone
    }
  }

  "contains" should {

    "confirm that a rectangle contains a point" in {
      sfoCalculator.contains(sfoRunwaySurface28L10R, sfoPointB) must beTrue
    }

    "confirm that a rectangle doesn't contain a point" in {
      sfoCalculator.contains(sfoRunwaySurface28L10R, sfoPointA) must beFalse
      sfoCalculator.contains(sfoRunwaySurface28L10R, sfoPointC) must beFalse
      sfoCalculator.contains(sfoRunwaySurface28L10R, sfoPointD) must beFalse
    }
  }

  "rotateAboutArbitraryOriginAndScaleToDistance" should {
    "calculate points" in {
      // Confirmed the following points' correctness visually, with Google Maps.
      sfoThresholdLeft28L must beCloseInTwoDimensionsTo(LongLat(-122.358510, 37.611469), significantFigures)
      sfoThresholdRight28L must beCloseInTwoDimensionsTo(LongLat(-122.358188, 37.611955), significantFigures)
      sfoThresholdLeft10R must beCloseInTwoDimensionsTo(LongLat(-122.392944, 37.626534), significantFigures)
      sfoThresholdRight10R must beCloseInTwoDimensionsTo(LongLat(-122.393267, 37.626048), significantFigures)
    }
  }

  "pointOnContinuouslyNearingSegmentAtDistance" should {

    "produce no point when the line containing the segment never passes sufficiently close to the reference point" in {
      val point = sfoCalculator.pointOnContinuouslyNearingSegmentAtDistance(pointG, pointJ, referencePoint, 25000)
      point must beNone
    }

    "produce no point when the line containing the segment is close enough, but the segment doesn't include a point at the specified distance" in {
      val point = sfoCalculator.pointOnContinuouslyNearingSegmentAtDistance(pointG, pointH, referencePoint, 60000)
      point must beNone
    }

    "produce a point when the line containing the segment is close enough and the segment includes a point at the specified distance" in {

      val point = sfoCalculator.pointOnContinuouslyNearingSegmentAtDistance(pointG, pointI, referencePoint, 60000)

      point must beSome
      point.get.angle.toCompassDegrees must beCloseTo(342.795570 within significantFigures)
      point.get.relativePosition must beCloseTo(0.981956 within significantFigures)

      // Calculated via sfoCalculator.pointAtAngleAndDistance() that the point at compass heading
      // 342.795570 deg. and 60,000 meters from the reference point is ~(-122.196443, 38.518049).
      // Confirmed via sfoCalculator.distanceInMeters() that that point is ~60,000 meters from the
      // reference point.
      //
      // Further calculated distances as follows, using sfoCalculator.distanceInMeters():
      //
      //     * Point G to (-122.196443, 38.518049): 110,295.9 meters
      //     * Point G to Point I:                  112,322.6 meters
      //
      // 110295.9 / 112322.6 is approximately equal to 0.981956.
    }

    // FIXME Morph this into a test case for the halfline method?
//    "produce 2 points when the line containing the segment is close enough and the segment includes both of the candidate points" in {
//
//      val points = sfoCalculator.pointOnApproachingLineSegmentAtDistance((pointG, pointJ), referencePoint, 26100)
//
//      points.length must beEqualTo(2)
//      points(0).angle.toCompassDegrees must beCloseTo(282.728709 within significantFigures)
//      points(0).percentageFromAToB must beCloseTo(0.964058 within significantFigures)
//      points(1).angle.toCompassDegrees must beCloseTo(274.007968 within significantFigures)
//      points(1).percentageFromAToB must beCloseTo(0.987613 within significantFigures)
//
//      // Calculated via sfoCalculator.pointAtAngleAndDistance() that the points at compass heading
//      // 282.728709 and 274.007968 deg. and 26,100 meters from the reference point are
//      // ~(-122.289441, 38.053935) and ~(-122.296363, 38.018588). Confirmed via
//      // sfoCalculator.distanceInMeters() that those points are ~26,100 meters from the reference
//      // point.
//      //
//      // Further calculated distances as follows, using sfoCalculator.distanceInMeters():
//      //
//      //     * Point G to (-122.289441, 38.053935): 162,433.7 meters
//      //     * Point G to (-122.296363, 38.018588): 166,402.5 meters
//      //     * Point G to Point J:                  168,489.5 meters
//      //
//      // 162433.7 / 168489.5 is approximately equal to 0.964058.
//      //
//      // 166402.5 / 168489.5 is approximately equal to 0.987613.
//    }
//
//    // TODO Cases for opposite angle? 90 degrees?
  }

  "continuouslyNears" should {

    "handle the simple nearing case: point B is closer to the reference point than point A is" in {
      sfoCalculator.continuouslyNears(pointG, pointH, referencePoint) must beTrue
    }

    "handle the simple non-nearing case: point B is farther from the reference point than point A is" in {
      sfoCalculator.continuouslyNears(pointH, pointG, referencePoint) must beFalse
    }

    "handle the complex non-nearing case: point B is closer to the reference point than point A is; but their segment includes the containing line's closest point to the reference point; so the sub-segment from that point to point B is non-nearing" in {
      sfoCalculator.continuouslyNears(pointG, pointJ, referencePoint) must beFalse
    }
  }
}
