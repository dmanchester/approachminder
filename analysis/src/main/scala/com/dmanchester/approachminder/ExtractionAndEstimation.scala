package com.dmanchester.approachminder

import com.dmanchester.approachminder.typeswithbehavior.MeanAngleAndAltitude
import com.dmanchester.approachminder.typeswithoutbehavior.AngleAndAltitude
import com.dmanchester.approachminder.utils.MathUtils

object ExtractionAndEstimation {

  def meanTrajectory(trajectories: Iterable[Map[BigDecimal, AngleAndAltitude]]): Map[BigDecimal, MeanAngleAndAltitude] = {

    // Collect the set of distances for which at least one trajectory has a position.
    val distancesInMeters = trajectories.flatMap(_.keys).toSet

    distancesInMeters.flatMap { thisDistance =>
      val positionsAtThisDistance = trajectories.flatMap(_.get(thisDistance))
      MathUtils.calculateMeanAngleAndAltitude(positionsAtThisDistance).map(thisDistance -> _)
    }.toMap
  }
}
