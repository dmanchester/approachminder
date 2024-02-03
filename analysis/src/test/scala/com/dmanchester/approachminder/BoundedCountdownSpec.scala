package com.dmanchester.approachminder

import org.specs2.mutable._

class BoundedCountdownSpec extends Specification {

  "an instance" should {

    "produce values *between* (but not including) the start and end bounds, if they're not evenly divisible by the step size" in {

      var countdown = BoundedCountdown(BigDecimal("2.6"), BigDecimal("1.9"), BigDecimal("0.5"))

      countdown.currentValueOption must beSome(BigDecimal("2.5"))

      countdown = countdown.next
      countdown.currentValueOption must beSome(BigDecimal("2.0"))

      countdown = countdown.next
      countdown.currentValueOption must beNone
    }

    "produce values *including* the start and/or end bounds, to the extent they're evenly divisible by the step size" in {

      var countdown = BoundedCountdown(BigDecimal("2.5"), BigDecimal("2.0"), BigDecimal("0.5"))

      countdown.currentValueOption must beSome(BigDecimal("2.5"))

      countdown = countdown.next
      countdown.currentValueOption must beSome(BigDecimal("2.0"))

      countdown = countdown.next
      countdown.currentValueOption must beNone
    }

    "produce no values if the start and end bounds sit between two adjacent candidate values" in {
      val countdown = BoundedCountdown(BigDecimal("2.3"), BigDecimal("2.2"), BigDecimal("0.5"))  // 2.3 and 2.2 are both between 2.5 and 2.0
      countdown.currentValueOption must beNone
    }
  }
}