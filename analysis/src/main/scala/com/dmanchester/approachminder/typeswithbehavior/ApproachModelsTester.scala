package com.dmanchester.approachminder.typeswithbehavior

import com.dmanchester.approachminder.typeswithoutbehavior.{HasLongLatAlt, ModelFit}

/**
 * A collection of approach models oriented toward testing trajectories against the models.
 *
 * Supports a maximum distance. A trajectory will not be tested against a model if the distance from the trajectory's
 * currentPosition to the threshold of the model's runway exceeds the maximum.
 *
 * The collection is ordered. This fact is not typically relevant, but in unusual cases, it can be. For more
 * information, see the documentation of testForBestFit()'s return value.
 *
 * @param models The models.
 * @param maxThresholdDistanceInMeters The maximum distance described above.
 */
case class ApproachModelsTester(models: Iterable[ApproachModel], maxThresholdDistanceInMeters: Double) {

  /**
   * Test a segment of a trajectory against the models (excluding those whose runways are more than
   * maxThresholdDistanceInMeters from the trajectory's currentPosition). Find the best fit: the model against which the
   * trajectory segment shows the smallest deviation.
   *
   * @param previousPosition The previous position in the trajectory.
   * @param currentPosition The current position in the trajectory.
   * @return The best fit, if any. -- In the unusual case that multiple models are found to have the best fit, the
   *         returned fit will reference the model that is first in this collection.
   */
  def testForBestFit(previousPosition: HasLongLatAlt, currentPosition: HasLongLatAlt): Option[ModelFit] = {

    models.filter { model =>
      model.runway.distanceInMetersToThresholdCenter(currentPosition) <= maxThresholdDistanceInMeters
    } map { model =>
      (model, model.testSegment(previousPosition, currentPosition))
    } collect {
      case (model, WithinRange(deviation, distanceTestedAtInMeters)) => (model, deviation, distanceTestedAtInMeters)
    } minByOption {
      _._2.normalizedEuclideanDistance
    } map { case (model, deviation, distanceTestedAtInMeters) =>
      ModelFit(model, deviation, distanceTestedAtInMeters)
    }
  }
}
