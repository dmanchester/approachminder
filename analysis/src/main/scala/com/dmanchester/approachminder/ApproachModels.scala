package com.dmanchester.approachminder

class ApproachModels private(models: Iterable[ApproachModel]) {

  // TODO Formalize a return type?
  def bestFit(previousPoint: HasLongLatAlt, currentPoint: HasLongLatAlt): Option[(ApproachModel, DeviationFromPositionDistribution, BigDecimal)] = {

    // TODO Could this be rewritten as a "for" comprehension?

    models.map { model =>
      (model.test(previousPoint, currentPoint), model)  // test the points against each model
    }.collect {
      case (WithinRange(deviation, appliedDistributionInMeters), model) => (model, deviation, appliedDistributionInMeters)  // collect the subset of test results that are WithinRange
    }.minByOption {
      case (_, deviation, _) => deviation.normalizedEuclideanDistance  // grab the one with the smallest normalized Euclidean distance
    }
  }
}

object ApproachModels {
  def apply(models: Iterable[ApproachModel]): ApproachModels = new ApproachModels(models)
}