package com.dmanchester.approachminder.utils

import com.dmanchester.approachminder.PolarAngles
import com.dmanchester.approachminder.typeswithbehavior.MeanAngleAndAltitude
import com.dmanchester.approachminder.typeswithoutbehavior.AngleAndAltitude
import org.apache.commons.math3.stat.StatUtils

import scala.math.sqrt

object ApproachModeling {

  /**
   * From a series of two or more positions, calculate the mean angle and altitude values. Includes standard deviation
   * for each value.
   *
   * @param positions The positions.
   * @return The means and standard deviations, as well as the count of positions included in the calculations, packaged
   *         in Some. -- Or, None if less than two positions were provided.
   */
  def calculateMeanAngleAndAltitude(positions: Iterable[AngleAndAltitude]): Option[MeanAngleAndAltitude] = {

    Option.when(positions.size >= 2) {

      val angles = positions.map(_.angle)
      val (meanAngle, angleStdDevInDegrees) = PolarAngles.circularMeanAndStdDevDegrees(angles)

      val altitudesMetersAsArray = positions.map(_.altitudeInMeters).toArray
      val meanAltitudeInMeters = StatUtils.mean(altitudesMetersAsArray)
      val altitudeStdDevInMeters = sqrt(StatUtils.variance(altitudesMetersAsArray, meanAltitudeInMeters))

      MeanAngleAndAltitude(meanAngle, angleStdDevInDegrees, meanAltitudeInMeters, altitudeStdDevInMeters, positions.size)
    }
  }

  /**
   * Calculate the mean trajectory from a collection of trajectories.
   *
   * @param trajectories The trajectories. Each one is a map of AngleAndAltitude values keyed by distance to a reference
   *                     point. -- Across trajectories, the distance values should rely on the same interval length
   *                     (e.g., 100 m), but the actual distance values can vary from one trajectory to another. Also,
   *                     the distance values within a single trajectory can be somewhat sparse (e.g., 800 m, 600 m,
   *                     500 m, 300 m).
   * @return The mean trajectory, also keyed by distance.
   */
  def meanTrajectory(trajectories: Iterable[Map[BigDecimal, AngleAndAltitude]]): Map[BigDecimal, MeanAngleAndAltitude] = {

    // Collect the set of distances for which at least one trajectory has a position.
    val distancesInMeters = trajectories.flatMap(_.keys).toSet

    distancesInMeters.flatMap { thisDistance =>
      val positionsAtThisDistance = trajectories.flatMap(_.get(thisDistance))
      calculateMeanAngleAndAltitude(positionsAtThisDistance).map(thisDistance -> _)
    }.toMap
  }
}
