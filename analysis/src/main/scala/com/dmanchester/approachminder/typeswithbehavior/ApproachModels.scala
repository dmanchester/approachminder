package com.dmanchester.approachminder.typeswithbehavior

import com.dmanchester.approachminder.typeswithoutbehavior.{HasLongLatAlt, ModelFit}

/**
 * A collection of approach models.
 *
 * The collection is ordered. This fact is not typically relevant, but in unusual cases, it can be. For more
 * information, see the documentation of testForBestFit()'s return value.
 *
 * @param models The models.
 */
case class ApproachModels(models: Iterable[ApproachModel]) {

  /**
   * Test a segment of a trajectory against the models. Find the best fit: the model against which the trajectory
   * segment shows the smallest deviation.
   *
   * @param previousPosition The previous position in the trajectory.
   * @param currentPosition The current position in the trajectory.
   * @return The best fit, if any. -- In the unusual case that multiple models are found to have the best fit, the
   *         returned fit will reference the model that is first in this collection.
   */
  def testForBestFit(previousPosition: HasLongLatAlt, currentPosition: HasLongLatAlt): Option[ModelFit] = {

    models.map { model =>
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
