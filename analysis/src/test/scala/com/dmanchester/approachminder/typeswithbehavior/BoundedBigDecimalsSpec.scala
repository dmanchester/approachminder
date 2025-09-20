package com.dmanchester.approachminder.typeswithbehavior

import org.specs2.mutable.*

class BoundedBigDecimalsSpec extends Specification {

  "valueLessThan" should {

    val bd_2_0 = BigDecimal("2.0")  // "bd" = "BigDecimal"
    val bd_4_7 = BigDecimal("4.7")
    val bd_5_9 = BigDecimal("5.9")

    val instance = BoundedBigDecimals(Set(bd_4_7, bd_5_9, bd_2_0), BigDecimal("0.7"))

    "handle a typical lookup" in {
      instance.valueLessThan(BigDecimal("5.7")) mustEqual WithinBounds(bd_4_7)
    }

    "handle a lookup of a contained value" in {
      instance.valueLessThan(bd_4_7) mustEqual WithinBounds(bd_2_0)
    }

    "handle a lookup of a value greater than the maximum contained value but under the upper bound" in {
      instance.valueLessThan(BigDecimal("6.1")) mustEqual WithinBounds(bd_5_9)
    }

    "handle a lookup at the upper bound" in {
      instance.valueLessThan(BigDecimal("6.6") /* 5.9 + 0.7 */) mustEqual GreaterThanOrEqualToUpperBound
    }

    "handle a lookup above the upper bound" in {
      instance.valueLessThan(BigDecimal("6.7")) mustEqual GreaterThanOrEqualToUpperBound
    }

    "handle a lookup at the lower bound" in {
      instance.valueLessThan(bd_2_0) mustEqual LessThanOrEqualToLowerBound
    }

    "handle a lookup below the lower bound" in {
      instance.valueLessThan(BigDecimal("1.9")) mustEqual LessThanOrEqualToLowerBound
    }
  }

  "newOption" should {

    "throw on empty 'values'" in {
      BoundedBigDecimals(Set.empty, BigDecimal("1.0")) must throwAn[IllegalArgumentException]
    }
  }
}