package com.dmanchester.approachminder.utils

import com.dmanchester.approachminder.data.SFOConstructs.*
import com.dmanchester.approachminder.data.Airports.sfoData
import com.dmanchester.approachminder.resources.TestHelpers.{beCloseInTwoDimensionsTo, significantFigures}
import com.dmanchester.approachminder.typeswithoutbehavior.{AngleAndRelativePosition, HasLongLat, LongLat}
import org.specs2.mutable.*

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

  private val referencePoint = LongLat(-122, 38)
  private val pointG = LongLat(-122, 39.5)
  private val pointH = LongLat(-122.1, 39)
  private val pointI = LongLat(-122.2, 38.5)
  private val pointJ = LongLat(-122.3, 38)

  "distanceInMeters" should {

    "calculate distance" in {
      val runway28L_lengthInMeters = sfoCalculator.distanceInMeters(sfoData.thresholdCenter28L, sfoData.thresholdCenter10R)

      // APT_RWY.csv gives length as 11381 ft., which converts to 3469 meters.
      runway28L_lengthInMeters must beCloseTo(3467.714078 within significantFigures)
    }
  }

  "intersection" should {

    val thresholdRunway28L = (sfoThresholdLeft28L, sfoThresholdRight28L)

    "calculate the point where two segments intersect" in {

      val intersection = sfoCalculator.intersection((sfoPointA, sfoPointB), thresholdRunway28L)

      // Calculated distances as follows, using sfoCalculator.distanceInMeters():
      //
      //     * Point A to Point F': 31.08932 meters
      //     * Point A to Point B:  89.36675 meters
      //
      // (Point F', at LongLat(-122.35838656432684,37.611655553983894), is a refinement of Point F. The extra decimal
      // places are needed to calculate the distance to six significant digits.)
      //
      // 31.08932 / 89.36675 is approximately equal to 0.347885.

      intersection must beSome { (theIntersection: (HasLongLat, Double)) =>

        val point = theIntersection._1
        val percentageFromFlightSegStartToSegEnd = theIntersection._2

        point must beCloseInTwoDimensionsTo(sfoPointF, significantFigures)
        percentageFromFlightSegStartToSegEnd must beCloseTo(0.347885 within significantFigures)
      }
    }

    "confirm that two segments don't intersect" in {
      val intersection = sfoCalculator.intersection((sfoPointA, sfoPointC), thresholdRunway28L)
      intersection must beNone
    }
  }

  "pointAlongSegment" should {

    "return the appropriate point" in {
      val point = sfoCalculator.pointAlongSegment((sfoData.thresholdCenter28L, sfoData.thresholdCenter10R), 0.25)
      // Confirmed the following point's correctness visually, with online map.
      point must beCloseInTwoDimensionsTo(LongLat(-122.367037, 37.615358), significantFigures)
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

  "rotateAboutAnOriginAndScaleToDistance" should {
    "calculate points" in {
      // Confirmed the following points' correctness visually, with online map.
      sfoThresholdLeft28L must beCloseInTwoDimensionsTo(LongLat(-122.358510, 37.611469), significantFigures)
      sfoThresholdRight28L must beCloseInTwoDimensionsTo(LongLat(-122.358188, 37.611955), significantFigures)
      sfoThresholdLeft10R must beCloseInTwoDimensionsTo(LongLat(-122.392944, 37.626534), significantFigures)
      sfoThresholdRight10R must beCloseInTwoDimensionsTo(LongLat(-122.393267, 37.626048), significantFigures)
    }
  }

  "continuouslyNears" should {

    "handle the simple nearing case: point B is closer to the reference point than point A is" in {
      sfoCalculator.continuouslyNears(pointG, pointH, referencePoint) must beTrue
    }

    "handle the simple non-nearing case: point B is farther from the reference point than point A is" in {
      sfoCalculator.continuouslyNears(pointH, pointG, referencePoint) must beFalse
    }

    "handle the complex non-nearing case: point B is closer to the reference point than point A is; but their segment includes the containing line's closest point to the reference point (so, the sub-segment from that point to point B is non-nearing)" in {
      sfoCalculator.continuouslyNears(pointG, pointJ, referencePoint) must beFalse
    }
  }

  "pointOnHalflineAtDistance" should {

    "produce a point when: the directed segment continuously nears the reference point; and, the line containing the directed segment passes the reference point within the specified distance" in {

      val point = sfoCalculator.pointOnHalflineAtDistance(pointG, pointI, referencePoint, 60000)

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

      point must beSome { (thePoint: AngleAndRelativePosition) =>
        thePoint.angle.asCompassDegrees must beCloseTo(342.795570 within significantFigures)
        thePoint.relativePosition must beCloseTo(0.981956 within significantFigures)
      }
    }

    "produce no point when: the directed segment continuously nears the reference point; but, the line containing the directed segment doesn't pass the reference point within the specified distance" in {
      val point = sfoCalculator.pointOnHalflineAtDistance(pointG, pointI, referencePoint, 20000)
      point must beNone
    }

    "throw when the directed segment doesn't continuously near the reference point" in {
      sfoCalculator.pointOnHalflineAtDistance(pointI, pointG, referencePoint, 60000) must throwAn[IllegalArgumentException]
    }
  }
}
