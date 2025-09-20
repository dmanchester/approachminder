package com.dmanchester.approachminder.typeswithbehavior

import scala.collection.Searching.{Found, InsertionPoint}

/**
 * A bounded set of BigDecimal values. Guaranteed to contain at least one value.
 *
 * Optimized for fast searching for the value less than a parameter. For example, given an instance containing 2.0, 4.7,
 * and 5.9:
 *
 *   - valueLessThan() on 5.7 produces 4.7.
 *   - valueLessThan() on 4.7 produces 2.0.
 *
 * This data structure implements the notion of bounds. A call to valueLessThan() with a parameter greater than or equal
 * to the upper bound receives back GreaterThanOrEqualToUpperBound. Similarly, a call to that method with a parameter
 * less than or equal to the lower bound receives back LessThanOrEqualToLowerBound.
 *
 * Not a case class. (This allows the class to keep its values storage--an implementation detail--out of its API.)
 */
class BoundedBigDecimals private(private val theValues: IndexedSeq[BigDecimal], val upperBound: BigDecimal) {
  val lowerBound: BigDecimal = theValues.min

  def valueLessThan(lookupValue: BigDecimal): ValueLessThanResult = {

    lookupValue match {

      case theLookupValue if theLookupValue <= lowerBound => LessThanOrEqualToLowerBound

      case theLookupValue if theLookupValue >= upperBound => GreaterThanOrEqualToUpperBound

      case theLookupValue =>
        val indexNextValue = theValues.search(theLookupValue) match {
          case Found(index) => index
          case InsertionPoint(index) => index
        }
        WithinBounds(theValues(indexNextValue - 1))
    }
  }
}

object BoundedBigDecimals {

  /**
   * Instantiate a BoundedBigDecimals.
   *
   * @param values The values to store. Must contain at least one value.
   * @param upperBoundOffsetFromMax The offset beyond the maximum value in "values" at which to set the upper bound.
   *                                (For example, if the maximum value is 13.0 and the offset is 4.5, the upper bound is
   *                                set to 17.0.)
   * @throws java.lang.IllegalArgumentException If "values" is empty.
   * @return The BoundedBigDecimals.
   */
  @throws(classOf[IllegalArgumentException])
  def apply(values: Set[BigDecimal], upperBoundOffsetFromMax: BigDecimal): BoundedBigDecimals = {

    if (values.isEmpty) {
      throw new IllegalArgumentException("'values' must not be empty!")
    }

    val theValues = values.toIndexedSeq.sorted
    val upperBound = theValues.max + upperBoundOffsetFromMax
    new BoundedBigDecimals(theValues, upperBound)
  }
}

sealed trait ValueLessThanResult
case class WithinBounds(value: BigDecimal) extends ValueLessThanResult
case object GreaterThanOrEqualToUpperBound extends ValueLessThanResult
case object LessThanOrEqualToLowerBound extends ValueLessThanResult
