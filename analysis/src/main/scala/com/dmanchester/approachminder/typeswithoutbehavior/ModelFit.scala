package com.dmanchester.approachminder.typeswithoutbehavior

import com.dmanchester.approachminder.typeswithbehavior.{ApproachModel, DeviationFromMean}

/**
 * How well a trajectory fits an approach model.
 *
 * @param model The approach model.
 * @param deviation The deviation found when the trajectory was tested against the model at a particular distance.
 * @param distanceTestedAtInMeters The distance at which the trajectory was tested.
 */
case class ModelFit(model: ApproachModel, deviation: DeviationFromMean, distanceTestedAtInMeters: BigDecimal)