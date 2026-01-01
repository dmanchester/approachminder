package com.dmanchester.approachminder.data

import com.dmanchester.approachminder.data.Airports.{sfo, sfoData}
import com.dmanchester.approachminder.typeswithbehavior.{ApproachModel, MeanAngleAndAltitude, PolarAngle}
import com.dmanchester.approachminder.typeswithoutbehavior.*
import com.dmanchester.approachminder.utils.GeographicCalculator
import com.dmanchester.approachminder.utils.MathUtils.feetToMeters

object SFOConstructs {

  val sfoCalculator = GeographicCalculator(sfoData.referencePoint)

  val sfoRunwayHalfWidthInMeters: Double = feetToMeters(sfoData.runwayWidthInFeet) / 2

  val sfoThresholdLeft28L: HasLongLat = sfoCalculator.rotateAboutAnOriginAndScaleToDistance(sfoData.thresholdCenter10R, sfoData.thresholdCenter28L, 90, sfoRunwayHalfWidthInMeters)
  val sfoThresholdRight28L: HasLongLat = sfoCalculator.rotateAboutAnOriginAndScaleToDistance(sfoData.thresholdCenter10R, sfoData.thresholdCenter28L, -90, sfoRunwayHalfWidthInMeters)
  val sfoThresholdLeft10R: HasLongLat = sfoCalculator.rotateAboutAnOriginAndScaleToDistance(sfoData.thresholdCenter28L, sfoData.thresholdCenter10R, 90, sfoRunwayHalfWidthInMeters)
  val sfoThresholdRight10R: HasLongLat = sfoCalculator.rotateAboutAnOriginAndScaleToDistance(sfoData.thresholdCenter28L, sfoData.thresholdCenter10R, -90, sfoRunwayHalfWidthInMeters)

  val sfoRunwaySurface28L10R = Polygon(Seq(sfoThresholdLeft28L, sfoThresholdRight28L, sfoThresholdLeft10R, sfoThresholdRight10R))

  // The points A - F are laid out with respect to runway 28L as follows (runway is area filled in
  // with dots):
  //
  // .E....\ D
  // .......\
  // ........\
  // \...B....\
  //  \......./
  //   \...../
  //    \...F
  //     \./  A
  //      V
  //
  //              C
  //
  // That is to say:
  //
  //   * Points B and E lie on the runway surface. Points A, C, and D do not.
  //   * Segments AB and AD cross runway the threshold. Segment AC does not.
  //   * Point F is segment AB's crossing of the threshold.
  //
  // Points A - E were chosen visually. Point F was calculated, with its correctness confirmed visually.
  val sfoPointA = LongLat(-122.358126, 37.611467)
  val sfoPointB = LongLat(-122.358875, 37.612009)
  val sfoPointC = LongLat(-122.357525, 37.611231)
  val sfoPointD = LongLat(-122.358551, 37.612304)
  val sfoPointE = LongLat(-122.359401, 37.612307)
  val sfoPointF = LongLat(-122.358387, 37.611656)

  val meanTrajectoryFromSoutheast: Map[BigDecimal, MeanAngleAndAltitude] = Map(
    BigDecimal("2000.0") -> MeanAngleAndAltitude(PolarAngle.fromCompassDegrees(119.60670416957628), 1.0, 60.0, 5.0, 1),
    BigDecimal("1000.0") -> MeanAngleAndAltitude(PolarAngle.fromCompassDegrees(120.38477105254175), 1.0, 50.0, 10.0, 1)
  )

  val sfoRunway28L: sfo.RunwaySurface#Runway = sfo.getRunwayByName("28L")

  val sfoRunway28LApproachModel: ApproachModel = ApproachModel.newOption(sfoRunway28L, sfoRunway28L.opposite.thresholdCenter, meanTrajectoryFromSoutheast).get

  val sfo28LApproachPointA = LongLatAlt(-122.359576105216, 37.5992826103728, 100.0)
  val sfo28LApproachPointB = LongLatAlt(-122.369627079319, 37.6044390332995, 90.0)
  val sfo28LApproachPointC = LongLatAlt(-122.379678053421, 37.6093993053915, 80.0)
  val sfo28LApproachPointD = LongLatAlt(-122.396155060146, 37.6157949574326, 70.0)
  val sfo28LApproachPointE = LongLatAlt(-122.394095434306, 37.6227773371242, 60.0)
}
