package com.dmanchester.approachminder

import Airports.sfoData

import org.specs2.matcher.Matchers.{SignificantFiguresSyntax, beCloseTo}
import org.specs2.matcher.{Matcher, SignificantFigures}

object SharedResources {

  // The number of significant figures to examine in specifications when checking floating-point
  // numbers.
  val significantFigures = 6.significantFigures

  // Some specifications rely on real-world data for SFO.

  val sfoCalculator = GeographicCalculator(sfoData.referencePoint)

  val sfoRunwayHalfWidthInMeters = Airports.feetToMetersConverter.convert(sfoData.runwayWidthInFeet) / 2

  val sfoThresholdLeft28L = sfoCalculator.rotateAboutArbitraryOriginAndScaleToDistance(sfoData.thresholdCenter10R, sfoData.thresholdCenter28L, 90, sfoRunwayHalfWidthInMeters)
  val sfoThresholdRight28L = sfoCalculator.rotateAboutArbitraryOriginAndScaleToDistance(sfoData.thresholdCenter10R, sfoData.thresholdCenter28L, -90, sfoRunwayHalfWidthInMeters)
  val sfoThresholdLeft10R = sfoCalculator.rotateAboutArbitraryOriginAndScaleToDistance(sfoData.thresholdCenter28L, sfoData.thresholdCenter10R, 90, sfoRunwayHalfWidthInMeters)
  val sfoThresholdRight10R = sfoCalculator.rotateAboutArbitraryOriginAndScaleToDistance(sfoData.thresholdCenter28L, sfoData.thresholdCenter10R, -90, sfoRunwayHalfWidthInMeters)

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
  // Points A - E were chosen visually. Point F was calculated by @@@, with its correctness confirmed visually.
  val sfoPointA = LongLat(-122.358126, 37.611467)
  val sfoPointB = LongLat(-122.358875, 37.612009)
  val sfoPointC = LongLat(-122.357525, 37.611231)
  val sfoPointD = LongLat(-122.358551, 37.612304)
  val sfoPointE = LongLat(-122.359401, 37.612307)
  val sfoPointF = LongLat(-122.358387, 37.611656)

  def beCloseInTwoDimensionsTo(expected: HasLongLat, figures: SignificantFigures): Matcher[HasLongLat] = {

    val longitudeTerm = beCloseTo(expected.longitude, figures) ^^ { (actual: HasLongLat) => actual.longitude }
    val latitudeTerm = beCloseTo(expected.latitude, figures) ^^ { (actual: HasLongLat) => actual.latitude }

    longitudeTerm and latitudeTerm
  }

  // TODO Is this used? If not, delete.
  def beCloseInThreeDimensionsTo(expected: HasLongLatAlt, figures: SignificantFigures): Matcher[HasLongLatAlt] = {

    val longitudeTerm = beCloseTo(expected.longitude, figures) ^^ { (actual: HasLongLatAlt) => actual.longitude }
    val latitudeTerm = beCloseTo(expected.latitude, figures) ^^ { (actual: HasLongLatAlt) => actual.latitude }
    val altitudeMetersTerm = beCloseTo(expected.altitudeMeters, figures) ^^ { (actual: HasLongLatAlt) => actual.altitudeMeters }

    longitudeTerm and latitudeTerm and altitudeMetersTerm
  }

  /**
   * Given a "simple" approach model (singular angle and altitude at various distances), mocks
   * corresponding distributions (from which a "real" `ApproachModel` can be constructed). The mean
   * angle and mean altitude of a distribution match the simple model at the distribution's distance.
   *
   * @param simpleApproachModel
   * @return
   */
  def mockApproachDistributions(simpleApproachModel /* maps distance to angle and altitude */ : Map[BigDecimal, (Double, Double)]): Map[BigDecimal, PositionDistribution] = {

    simpleApproachModel.map { case (distanceInMeters, (polarAngleCompassDegrees, altitudeMeters)) =>

      val distribution = PositionDistribution.fromDataOption(Seq(
        AngleAndAltitude(PolarAngle.fromCompassDegrees(polarAngleCompassDegrees - 1.0), altitudeMeters - 10.0),
        AngleAndAltitude(PolarAngle.fromCompassDegrees(polarAngleCompassDegrees + 1.0), altitudeMeters + 10.0)
      )).get

      (distanceInMeters, distribution)
    }
  }
}
