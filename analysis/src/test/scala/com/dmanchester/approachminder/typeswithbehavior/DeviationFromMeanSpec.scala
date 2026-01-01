package com.dmanchester.approachminder.typeswithbehavior

import com.dmanchester.approachminder.resources.TestHelpers.significantFigures
import org.specs2.mutable.*

class DeviationFromMeanSpec extends Specification {

  "constructor" should {

    "calculate a finite normalized Euclidean distance" in {
      DeviationFromMean(0.0, 5.6, 0.0, 7.8).normalizedEuclideanDistance must beCloseTo(9.602083 within significantFigures)
    }

    "calculate an infinite normalized Euclidean distance" in {
      DeviationFromMean(0.0, Double.PositiveInfinity, 0.0, 7.8).normalizedEuclideanDistance.isInfinity must beTrue
    }
  }
}