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
}
