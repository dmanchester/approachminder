package com.dmanchester.approachminder

import com.dmanchester.approachminder.typeswithbehavior.PolarAngle

import scala.math.*

object PolarAngles {

  private def circularMean(angles: Iterable[PolarAngle]): PolarAngle = {

    val anglesRadians = angles.map(_.asRadians)
    val sineTerm = anglesRadians.map(sin).sum
    val cosineTerm = anglesRadians.map(cos).sum

    val meanRadians = atan2(sineTerm, cosineTerm)
    PolarAngle.fromRadians(meanRadians)
  }

  /**
   * Calculate the circular mean and standard deviation of a collection of angles.
   *
   * For more information on the circular mean, see https://en.wikipedia.org/wiki/Circular_mean.
   *
   * At least two angles are required.
   *
   * @param angles The angles.
   * @throws java.lang.IllegalArgumentException If less than two angles are provided.
   * @return The circular mean and standard deviation.
   */
  @throws(classOf[IllegalArgumentException])
  def circularMeanAndStdDevDegrees(angles: Iterable[PolarAngle]): (PolarAngle, Double) = {

    if (angles.size < 2) {
      throw new IllegalArgumentException(s"Need at least 2 angles; got ${angles.size}!")
    }

    val thisCircularMean = circularMean(angles)

    // Complementing "circular mean", there is also "circular standard deviation":
    // https://en.wikipedia.org/wiki/Directional_statistics#Standard_deviation
    // However, it is not a particularly intuitive measure, and the appropriateness of simply
    // swapping it in for traditional standard deviation--for example, in normalized Euclidean
    // distance--is unclear.
    //
    // Thus, we rely on traditional standard deviation.
    //
    // That it is acceptable to use traditional standard deviation with polar angles seems to be
    // borne out by, for example, K.A. Verrall and R.L. Williams' 1982 paper "A Method for
    // Estimating the Standard Deviation of Wind Directions".
    //
    // That paper happens to propose a polar-angle alternative to traditional standard deviation,
    // but only because of the difficulties of calculating it on a continuous basis from streamed
    // readings when storage space is limited.

    val squaredDifferencesFromMeanDegrees = angles.map { angle =>
      val differenceFromMeanDegrees = angle.minusAsDegrees(thisCircularMean)
      pow(differenceFromMeanDegrees, 2)
    }

    val stdDevDegrees = sqrt(squaredDifferencesFromMeanDegrees.sum / (angles.size - 1))

    (thisCircularMean, stdDevDegrees)
  }
}
