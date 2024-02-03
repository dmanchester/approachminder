package com.dmanchester.approachminder

class ApproachModels private(models: Iterable[ApproachModel]) {

  // TODO Formalize a return type? Also, switch order within ApproachModel.WithinRange? Distance first seems dopey.
  def bestFit(previousPoint: HasLongLatAlt, currentPoint: HasLongLatAlt): Option[(ApproachModel, BigDecimal, DeviationFromPositionDistribution)] = {

    // TODO Could this be rewritten as a "for" comprehension?

    models.map { model =>
      (model.test(previousPoint, currentPoint), model)  // test the points against each model
    }.collect {
      case (WithinRange(appliedDistributionInMeters, deviation), model) => (model, appliedDistributionInMeters, deviation)  // collect the subset of test results that are WithinRange
    }.minByOption {
      case (_, _, deviation) => deviation.normalizedEuclideanDistance  // grab the one with the smallest normalized Euclidean distance
    }
  }
}

object ApproachModels {
  def apply(models: Iterable[ApproachModel]): ApproachModels = new ApproachModels(models)
}