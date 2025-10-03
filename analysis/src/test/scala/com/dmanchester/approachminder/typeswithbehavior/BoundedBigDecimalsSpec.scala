package com.dmanchester.approachminder.typeswithbehavior

import org.specs2.mutable.*

class BoundedBigDecimalsSpec extends Specification {

  private val bd_2_0 = BigDecimal("2.0")  // "bd" = "BigDecimal"
  private val bd_4_7 = BigDecimal("4.7")
  private val bd_5_9 = BigDecimal("5.9")

  "valueLessThanOrEqualTo" should {

    val bd_6_6 = BigDecimal("6.6")

    val instance = BoundedBigDecimals(Set(bd_4_7, bd_5_9, bd_2_0), bd_6_6)

    "handle a typical lookup" in {
      instance.valueLessThanOrEqualTo(BigDecimal("5.7")) mustEqual WithinBounds(bd_4_7)
    }

    "handle a lookup of a contained value" in {
      instance.valueLessThanOrEqualTo(bd_4_7) mustEqual WithinBounds(bd_4_7)
    }

    "handle a lookup of a value greater than the maximum contained value but under the upper bound" in {
      instance.valueLessThanOrEqualTo(BigDecimal("6.1")) mustEqual WithinBounds(bd_5_9)
    }

    "handle a lookup at the upper bound" in {
      instance.valueLessThanOrEqualTo(bd_6_6) mustEqual WithinBounds(bd_5_9)
    }

    "handle a lookup above the upper bound" in {
      instance.valueLessThanOrEqualTo(BigDecimal("6.7")) mustEqual GreaterThanUpperBound
    }

    "handle a lookup at the lower bound" in {
      instance.valueLessThanOrEqualTo(bd_2_0) mustEqual WithinBounds(bd_2_0)
    }

    "handle a lookup below the lower bound" in {
      instance.valueLessThanOrEqualTo(BigDecimal("1.9")) mustEqual LessThanLowerBound
    }
  }

  "newOption" should {

    "throw on empty 'values'" in {
      BoundedBigDecimals(Set.empty, BigDecimal("1.0")) must throwAn[IllegalArgumentException]
    }
  }

  "throw on upper bound less than lower bound" in {
    BoundedBigDecimals(Set(bd_4_7, bd_5_9), bd_2_0) must throwAn[IllegalArgumentException]  // TODO Examine IAE message
  }
}