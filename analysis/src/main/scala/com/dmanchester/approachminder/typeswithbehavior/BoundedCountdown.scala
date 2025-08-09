package com.dmanchester.approachminder.typeswithbehavior

/**
 * Facilitates counting down through values until a bound is reached/exceeded.
 *
 * A BoundedCountdown instance has a currentValue. When the next value is desired, client code calls next().
 *
 * If currentValue minus stepSize is still greater than endBound, or is equal to it, next() returns a new
 * BoundedCountdown with that as the currentValue, wrapping it in Some.
 *
 * Conversely, if that subtraction produces a value less than endBound, next() returns() None.
 *
 * Not a case class. (This allows class's API to consist only of currentValue and next().)
 */
class BoundedCountdown private(val currentValue: BigDecimal, private val endBound: BigDecimal, private val stepSize: BigDecimal) {

  def next: Option[BoundedCountdown] = BoundedCountdown.newOption(currentValue - stepSize, endBound, stepSize)
}

object BoundedCountdown {

  def newOption(currentValue: BigDecimal, endBound: BigDecimal, stepSize: BigDecimal): Option[BoundedCountdown] = {
    Option.when(currentValue >= endBound)(new BoundedCountdown(currentValue, endBound, stepSize))
  }
}