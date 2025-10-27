package com.dmanchester.approachminder.utils

import com.dmanchester.approachminder.typeswithbehavior.{ApproachModel, ApproachModelsTester, Trajectory}
import com.dmanchester.approachminder.typeswithoutbehavior.{HasLongLatAlt, ModelFitWithDisplayFields}

object TrajectoryTesting {

  /**
   * Test the segments of a trajectory against a series of models. For each segment, find the best fit (if any): the
   * model against which the segment shows the smallest deviation.
   *
   * Segments often do not fit any model: for example, a segment may be out of range of all models.
   *
   * @param approachModels The models.
   * @param trajectory The trajectory.
   * @param maxThresholdDistanceInMeters The maximum distance that can exist between a segment's more-recent position
   *                                     and a runway threshold if this method is to test the segment against a model
   *                                     for the runway.
   * @param maxNormalizedEuclideanDistance The maximum normalized Euclidean distance that a fit can exhibit between a
   *                                       model and a segment for this method to retain that fit. (Fits exceeding this
   *                                       maximum are discarded.)
   * @tparam P The type of the trajectory's positions.
   * @return The trajectory, with each position supplemented with an Option[ModelFitWithDisplayFields] representing the
   *         best fit (if any).
   */
  def testForBestFits[P <: HasLongLatAlt](approachModels: Iterable[ApproachModel], trajectory: Trajectory[P], maxThresholdDistanceInMeters: Int, maxNormalizedEuclideanDistance: Double): Trajectory[(P, Option[ModelFitWithDisplayFields])] = {

    val approachModelsTester = ApproachModelsTester(approachModels, maxThresholdDistanceInMeters)

    trajectory.mapPositionsByIndex { case (positions, index) =>

      val currentPosition = positions(index)

      val bestFitWithDisplayFields = if (index == 0) {
        None
      } else {
        val previousPosition = positions(index - 1)
        val bestFit = approachModelsTester.testForBestFit(previousPosition, currentPosition)
        bestFit filter { theFit =>
          theFit.deviation.normalizedEuclideanDistance <= maxNormalizedEuclideanDistance
        } map { theFit =>
          val thresholdDistanceInMeters = theFit.model.runway.distanceInMetersToThresholdCenter(currentPosition)
          val horizontalDevInMeters = MathUtils.isoscelesBaseLength(theFit.deviation.angleDevInDegrees, theFit.distanceTestedAtInMeters.toDouble)
          ModelFitWithDisplayFields(theFit, thresholdDistanceInMeters, horizontalDevInMeters)
        }
      }

      (currentPosition, bestFitWithDisplayFields)
    }
  }
}
