package com.dmanchester.approachminder.typeswithbehavior

import com.dmanchester.approachminder.SharedResources.significantFigures
import org.specs2.mutable.*

class DeviationFromMeanSpec extends Specification {

  "constructor" should {

    "calculate normalized Euclidean distance" in {
      DeviationFromMean(0.0, 5.6, 0.0, 7.8).normalizedEuclideanDistance must beCloseTo(9.602083 within significantFigures)
    }
  }
}