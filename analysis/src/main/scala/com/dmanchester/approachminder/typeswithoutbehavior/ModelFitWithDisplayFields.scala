package com.dmanchester.approachminder.typeswithoutbehavior

/**
 * A ModelFit instance, along with some display-oriented fields.
 *
 * @param modelFit The ModelFit.
 * @param thresholdDistanceInMeters The distance from the position at which a trajectory was tested to the runway
 *                                  threshold of the ModelFit's ApproachModel.
 * @param horizontalDevInMeters The horizontal deviation between the tested position and the ApproachModel.
 */
case class ModelFitWithDisplayFields(modelFit: ModelFit, thresholdDistanceInMeters: Double, horizontalDevInMeters: Double)
