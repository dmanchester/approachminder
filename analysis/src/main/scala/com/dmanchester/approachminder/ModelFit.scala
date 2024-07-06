package com.dmanchester.approachminder

class ModelFit private(val model: ApproachModel, val deviation: AngleAndAltitudeDeviation, val appliedDistributionInMeters: BigDecimal)

object ModelFit {
  def apply(model: ApproachModel, deviation: AngleAndAltitudeDeviation, appliedDistributionInMeters: BigDecimal): ModelFit = new ModelFit(model, deviation, appliedDistributionInMeters)
}