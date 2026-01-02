package com.dmanchester.approachminder.resources

import com.dmanchester.approachminder.typeswithbehavior.Trajectory
import com.dmanchester.approachminder.typeswithoutbehavior.{HasLongLat, HasLongLatAlt}
import org.specs2.matcher.Matchers.{SignificantFiguresSyntax, beCloseTo}
import org.specs2.matcher.{Matcher, SignificantFigures}

object TestHelpers {

  // The number of significant figures to examine in specifications/tests when checking floating-point numbers.
  val significantFigures: SignificantFigures = 6.significantFigures

  // A specs2 Matcher.
  def beCloseInTwoDimensionsTo(expected: HasLongLat, figures: SignificantFigures): Matcher[HasLongLat] = {

    val longitudeTerm = beCloseTo(expected.longitude, figures) ^^ { (actual: HasLongLat) => actual.longitude }
    val latitudeTerm = beCloseTo(expected.latitude, figures) ^^ { (actual: HasLongLat) => actual.latitude }

    longitudeTerm and latitudeTerm
  }

  // Another specs2 Matcher.
  def beCloseInThreeDimensionsTo(expected: HasLongLatAlt, figures: SignificantFigures): Matcher[HasLongLatAlt] = {
    val altitudeMetersTerm = beCloseTo(expected.altitudeMeters, figures) ^^ { (actual: HasLongLatAlt) => actual.altitudeMeters }
    beCloseInTwoDimensionsTo(expected, figures) and altitudeMetersTerm
  }

  def trajectoryFromPositions[P](positions: Seq[P]): Trajectory[P] = {
    Trajectory.newOption(positions, "(icao24)", Some("(callsign)"), None).get
  }
}
