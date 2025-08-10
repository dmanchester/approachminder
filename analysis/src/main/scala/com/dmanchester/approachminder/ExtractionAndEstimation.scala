package com.dmanchester.approachminder

import com.dmanchester.approachminder.typeswithbehavior.{BoundedCountdown, ContinuouslyNearingTrajectory}
import com.dmanchester.approachminder.typeswithoutbehavior.{AngleAndAltitude, HasLongLatAlt}
import com.dmanchester.approachminder.utils.MathUtils

import scala.annotation.tailrec

object ExtractionAndEstimation {

  def meanTrajectory(trajectories: Iterable[Map[BigDecimal, AngleAndAltitude]]): Map[BigDecimal, AngleAndAltitudeWithStats] = {

    // Collect the set of distances for which at least one trajectory has a position.
    val distancesInMeters = trajectories.map(_.keys).toSet.flatten

    distancesInMeters.flatMap { thisDistance =>

      val positionsAtThisDistance = trajectories.flatMap(_.get(thisDistance))
      val angleAndAltitudeWithStatsOption = AngleAndAltitudeWithStats.fromDataOption(positionsAtThisDistance)

      // If it was possible to create an AngleAndAltitudeWithStats at this distance (generally, that
      // hinges on whether there were at least two positions), queue up a `Map` entry, mapping this
      // distance to that distribution.
      angleAndAltitudeWithStatsOption.map(thisDistance -> _)

    }.toMap
  }
}
