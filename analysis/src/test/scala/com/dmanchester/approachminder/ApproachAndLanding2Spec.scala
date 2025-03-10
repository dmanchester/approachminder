package com.dmanchester.approachminder

import com.dmanchester.approachminder.Airports.sfo
import com.dmanchester.approachminder.SharedResources.*
import org.specs2.mutable.*

class ApproachAndLanding2Spec extends Specification {

  // The test-data points, A - J, are laid out relative to SFO runway 10L as follows:
  //
  // B
  //
  //
  //    C     A
  //
  //      /\
  //      \D\
  //       \ \
  //        \ \
  //         \E\
  //          \ \
  //           \ \
  //            \F\
  //             \ \
  //              \ \
  //             J \G\
  //                \ \
  //                 \ \
  //                  \H\
  //                   \/
  //
  //                      I
  //
  // Points B - I coincide with the runway centerline. They are placed per the table below. The percentage indicates
  // their position relative to the runway threshold; 0% = on the runway 10L threshold; 100% = on the opposite threshold
  // (i.e., that of runway 28R). So, points B, C, and I are not on the runway surface.
  //
  //     Point |  %
  //     ------+------
  //       B   | -30%
  //       C   | -15%
  //       D   |  15%
  //       E   |  30%
  //       F   |  50%
  //       G   |  70%
  //       H   |  90%
  //       I   | 110%
  //
  // Point A has the longitude of point E and the latitude of point C.
  //
  // Point J has the longitude of point F. Its latitude is just greater than that of point G. (In attempting to use
  // exactly point G's latitude, point J appeared to be ever so slightly south of point G. Segment F-J then no longer
  // continuously neared point G.)

  private val sfoThreshold10L = sfo.thresholdByName("10L").get

  private val pointB = LongLatAlt(-122.404270, 37.633298, 40)
  private val pointC = LongLatAlt(-122.398831, 37.631019, 40)
  private val pointD = LongLatAlt(-122.387953, 37.626459, 30)
  private val pointE = LongLatAlt(-122.382515, 37.624178, 10)
  private val pointF = LongLatAlt(-122.375265, 37.621138, 0)
  private val pointG = LongLatAlt(-122.368015, 37.618096, 0)
  private val pointH = LongLatAlt(-122.360766, 37.615055, 0)
  private val pointI = LongLatAlt(-122.353517, 37.612012, 0)

  private val pointA = LongLatAlt(pointE.longitude, pointC.latitude, 40)
  private val pointJ = LongLatAlt(pointF.longitude, pointG.latitude + 0.000001, 0)  // See discussion above of point G's latitude.

  private val trajectoryCE = trajectory3FromPositions(Seq(pointC, pointE))
  private val thresholdAndRefPointG = ThresholdAndReferencePoint(sfoThreshold10L, pointG)

  "createOption" should {

    "handle the simplest possible `Some` case, which involves a two-point threshold-crossing trajectory (trajectory C-E) continuously nearing a reference point" in {

      val (approachAndLanding, segmentsIncludedAfterSpecified) = ApproachAndLanding2.createOption(trajectoryCE, 0, thresholdAndRefPointG).get
      approachAndLanding.trajectory.positions mustEqual(Seq(pointC, pointE))
      approachAndLanding.threshold mustEqual(sfoThreshold10L)  // the same for all `Some` tests; we only bother to confirm it here
      approachAndLanding.crossingPointInterpolated must beCloseInTwoDimensionsTo(sfoThreshold10L.center, significantFigures)  // Because C and E are on the runway centerline, they cross the threshold at its center point
      approachAndLanding.crossingPointInterpolated.altitudeMeters must beCloseTo(29.999618, significantFigures)  // crossing point is 1/3 of the way from C to E; altitude is thus 1/3 of the descent from C (40 m) to E (10 m)

      segmentsIncludedAfterSpecified mustEqual(0)
    }

    "return `None` for trajectory C-E if that trajectory passes the reference point (and thus does not continuously near it)" in {
      val thresholdAndRefPoint = ThresholdAndReferencePoint(sfoThreshold10L, pointD)
      ApproachAndLanding2.createOption(trajectoryCE, 0, thresholdAndRefPoint) must beNone
    }

    "return `None` for a two-point trajectory that crosses the threshold inbound but ends outside the runway surface" in {
      val trajectory = trajectory3FromPositions(Seq(pointC, pointI))
      ApproachAndLanding2.createOption(trajectory, 0, thresholdAndRefPointG) must beNone
    }

    "return `None` for a two-point trajectory that ends inside the runway surface but does not cross the threshold" in {
      val trajectory = trajectory3FromPositions(Seq(pointA, pointE))
      ApproachAndLanding2.createOption(trajectory, 0, thresholdAndRefPointG) must beNone
    }

    "apply additional segments *before* the threshold-crossing one, provided they continuously near the reference point" in {
      val trajectory = trajectory3FromPositions(Seq(pointB, pointC, pointE))

      val (approachAndLanding, _) = ApproachAndLanding2.createOption(trajectory, 1, thresholdAndRefPointG).get
      approachAndLanding.trajectory.positions mustEqual Seq(pointB, pointC, pointE)
    }

    "apply additional segments *before* the threshold-crossing one, provided they continuously near the reference point; but stop when they no longer do" in {
      val trajectory = trajectory3FromPositions(Seq(pointA, pointB, pointC, pointE))

      val (approachAndLanding, _) = ApproachAndLanding2.createOption(trajectory, 2, thresholdAndRefPointG).get
      approachAndLanding.trajectory.positions mustEqual Seq(pointB, pointC, pointE)
    }

    "apply additional segments *after* the threshold-crossing one, provided they continuously near the reference point and are on the runway surface" in {
      val trajectory = trajectory3FromPositions(Seq(pointC, pointE, pointF))

      val (approachAndLanding, segmentsIncludedAfterSpecified) = ApproachAndLanding2.createOption(trajectory, 0, thresholdAndRefPointG).get
      approachAndLanding.trajectory.positions mustEqual Seq(pointC, pointE, pointF)
      segmentsIncludedAfterSpecified mustEqual(1)
    }

    "apply additional segments *after* the threshold-crossing one, provided they continuously near the reference point and are on the runway surface; but stop once they no longer near the reference point" in {
      val trajectory = trajectory3FromPositions(Seq(pointC, pointE, pointF, pointH))

      val (approachAndLanding, segmentsIncludedAfterSpecified) = ApproachAndLanding2.createOption(trajectory, 0, thresholdAndRefPointG).get
      approachAndLanding.trajectory.positions mustEqual Seq(pointC, pointE, pointF)
      segmentsIncludedAfterSpecified mustEqual(1)
    }

    "apply additional segments *after* the threshold-crossing one, provided they continuously near the reference point and are on the runway surface; but stop once they leave the runway surface" in {
      val trajectory = trajectory3FromPositions(Seq(pointC, pointE, pointF, pointJ))

      val (approachAndLanding, segmentsIncludedAfterSpecified) = ApproachAndLanding2.createOption(trajectory, 0, thresholdAndRefPointG).get
      approachAndLanding.trajectory.positions mustEqual Seq(pointC, pointE, pointF)
      segmentsIncludedAfterSpecified mustEqual(1)
    }
  }
}