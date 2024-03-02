package com.dmanchester.approachminder

class ApproachModels private(models: Iterable[ApproachModel]) {

  def bestFit(previousPoint: HasLongLatAlt, currentPoint: HasLongLatAlt): Option[ModelFit] = {

    // TODO Could this be rewritten as a "for" comprehension?

    models.map { model =>
      (model.test(previousPoint, currentPoint), model)  // test the points against each model
    }.collect {
      case (WithinRange(deviation, appliedDistributionInMeters), model) => ModelFit(model, deviation, appliedDistributionInMeters)  // collect the subset of test results that are WithinRange
    }.minByOption {
      _.deviation.normalizedEuclideanDistance  // grab the ModelFit whose deviation has the smallest normalized Euclidean distance
    }
  }
}

object ApproachModels {
  def apply(models: Iterable[ApproachModel]): ApproachModels = new ApproachModels(models)
}