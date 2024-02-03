package com.dmanchester.approachminder

/**
 * Counts down through values between a start bound and an end bound.
 *
 * The values, wrapped in `Some`, occur at fixed steps between the bounds.
 *
 * The first value is the largest number evenly divisible by the step size that is less than or
 * equal to the start bound.
 *
 * The last value is the smallest number evenly divisible by the step size that is greater than or
 * equal to the end bound.
 *
 * Once counting has progressed past the end bound, the current value becomes `None`.
 *
 * @param currentValue
 * @param endBound
 * @param stepSize
 */
class BoundedCountdown private(private val currentValue: BigDecimal, private val endBound: BigDecimal, private val stepSize: BigDecimal) {

  val currentValueOption = if (currentValue >= endBound) {
    Some(currentValue)
  } else {
    None
  }

  def next: BoundedCountdown = new BoundedCountdown(currentValue - stepSize, endBound, stepSize)
}

object BoundedCountdown {

  def apply(startBound: BigDecimal, endBound: BigDecimal, stepSize: BigDecimal): BoundedCountdown = {

    val (divisionIntegralValue, _) = startBound /% stepSize
    val currentValue = stepSize * divisionIntegralValue

    new BoundedCountdown(currentValue, endBound, stepSize)
  }
}