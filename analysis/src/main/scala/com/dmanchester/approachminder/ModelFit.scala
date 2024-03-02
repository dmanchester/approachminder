package com.dmanchester.approachminder

class ModelFit private(val model: ApproachModel, val deviation: DeviationFromPositionDistribution, val appliedDistributionInMeters: BigDecimal)

object ModelFit {
  def apply(model: ApproachModel, deviation: DeviationFromPositionDistribution, appliedDistributionInMeters: BigDecimal): ModelFit = new ModelFit(model, deviation, appliedDistributionInMeters)
}