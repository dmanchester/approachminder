package com.dmanchester.approachminder.typeswithbehavior

import org.specs2.mutable.*

class BoundedCountdownSpec extends Specification {

  "a series of BoundedCountdown instances" should {

    "produce values until crossing endBound" in {

      var countdown = BoundedCountdown.newOption(BigDecimal("2.6"), BigDecimal("1.9"), BigDecimal("0.5"))

      countdown.get.currentValue mustEqual BigDecimal("2.6")

      countdown = countdown.get.next
      countdown.get.currentValue mustEqual BigDecimal("2.1")

      countdown = countdown.get.next
      countdown must beNone
    }

    "produce values until equaling endBound" in {

      var countdown = BoundedCountdown.newOption(BigDecimal("2.9"), BigDecimal("1.9"), BigDecimal("0.5"))

      countdown = countdown.get.next
      countdown = countdown.get.next
      countdown.get.currentValue mustEqual BigDecimal("1.9")

      countdown = countdown.get.next
      countdown must beNone
    }

    "BoundedCountdown.newOption()" should {

      "not create an instance if currentValue is less than endBound" in {

        val countdown = BoundedCountdown.newOption(BigDecimal("1.8"), BigDecimal("1.9"), BigDecimal("0.5"))
        countdown must beNone
      }
    }
  }
}