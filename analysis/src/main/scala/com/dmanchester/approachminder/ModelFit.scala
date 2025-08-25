package com.dmanchester.approachminder

import com.dmanchester.approachminder.typeswithbehavior.DeviationFromMean

case class ModelFit(model: ApproachModel, deviation: DeviationFromMean, appliedDistributionInMeters: BigDecimal)