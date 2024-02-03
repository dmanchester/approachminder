package com.dmanchester.approachminder

import org.specs2.mutable._

class IntervalsSpec extends Specification {

  val intervalBoundaries = Set(BigDecimal("1.1"), BigDecimal("-3.2"), BigDecimal("6.0"))
  val intervals = Intervals.fromPointsSet(intervalBoundaries)

  "search" should {

    "return Between when appropriate" in {
      val result = intervals.search(BigDecimal("2.5"))
      result must beLike {
        case BetweenPoints(a, b) => {
          a must beEqualTo(BigDecimal("1.1"))
          b must beEqualTo(BigDecimal("6.0"))
        }
      }
    }

    "return EqualTo when appropriate" in {
      val result = intervals.search(BigDecimal("-3.2"))
      result must beEqualTo(MatchesAPoint)
    }

    "return LessThanMin when appropriate" in {
      val result = intervals.search(BigDecimal("-3.3"))
      result must beEqualTo(LessThanMin)
    }

    "return GreaterThanMax when appropriate" in {
      val result = intervals.search(BigDecimal("7.0"))
      result must beEqualTo(GreaterThanMax)
    }
  }
}