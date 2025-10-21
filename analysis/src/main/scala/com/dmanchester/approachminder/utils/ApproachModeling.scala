package com.dmanchester.approachminder.utils

import com.dmanchester.approachminder.PolarAngles
import com.dmanchester.approachminder.typeswithbehavior.{ApproachAndLanding, ApproachModel, MeanAngleAndAltitude}
import com.dmanchester.approachminder.typeswithoutbehavior.{AngleAndAltitude, HasLongLatAlt}
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
  def meanAngleAndAltitude(positions: Iterable[AngleAndAltitude]): Option[MeanAngleAndAltitude] = {

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
      meanAngleAndAltitude(positionsAtThisDistance).map(thisDistance -> _)
    }.toMap
  }

  /**
   * From a series of ApproachAndLanding instances, construct a series of ApproachModel instances.
   *
   * This method begins by grouping the ApproachAndLanding instances by runway and reference point. In each group, it
   * samples the instances' trajectories at the given interval length. It then calculates a mean trajectory.
   *
   * That mean trajectory is the basis for the model for that runway and reference point.
   *
   * @param approachesAndLandings The ApproachAndLanding instances.
   * @param intervalLengthInMeters The interval length to sample at, in meters.
   * @return The approach models. -- Packaged as an Iterable[ApproachModel] as opposed to an ApproachModels. Doing so
   *         allows client code of this method to apply an ordering to the models before packaging them as an
   *         ApproachModels.
   */
  def constructModels(approachesAndLandings: Iterable[ApproachAndLanding[HasLongLatAlt]], intervalLengthInMeters: BigDecimal): Iterable[ApproachModel] = {

    val approachesAndLandingsByRunwayAndRefPoint = approachesAndLandings.groupBy { approachAndLanding =>
      (approachAndLanding.runway, approachAndLanding.trajectory.referencePoint)
    }

    approachesAndLandingsByRunwayAndRefPoint.flatMap { case (runwayAndRefPoint, approachesAndLandingsThisRunwayAndRefPoint) =>
      val interpolatedTrajectories = approachesAndLandingsThisRunwayAndRefPoint.map { approachAndLanding =>
        TrajectoryUtils.interpolateAtIntervals(approachAndLanding.trajectory, intervalLengthInMeters).positions
      }

      val theMeanTrajectory = meanTrajectory(interpolatedTrajectories)

      ApproachModel.newOption(runwayAndRefPoint._1, runwayAndRefPoint._2, theMeanTrajectory)
    }
  }
}
