package com.dmanchester.approachminder.typeswithbehavior

import scala.collection.Searching.{Found, InsertionPoint}

/**
 * A bounded set of BigDecimal values. Guaranteed to contain at least one value.
 *
 * Optimized for fast searching for the value less than or equal to a lookup value.
 *
 * This class implements the notion of bounds. A call to valueLessThanOrEqualTo() with a lookup value greater than the
 * upper bound receives back GreaterThanUpperBound. Similarly, a call to that method with a lookup value less than the
 * lower bound receives back LessThanLowerBound.
 *
 * Not a case class. (This allows the class to keep its values storage--an implementation detail--out of its API.)
 */
class BoundedBigDecimals private(private val values: IndexedSeq[BigDecimal], val lowerBound: BigDecimal, val upperBound: BigDecimal) {

  def valueLessThanOrEqualTo(lookupValue: BigDecimal): ValueLessThanOrEqualToResult = {

    lookupValue match {

      case theLookupValue if theLookupValue < lowerBound => LessThanLowerBound

      case theLookupValue if theLookupValue > upperBound => GreaterThanUpperBound

      case theLookupValue =>
        val indexToUse = values.search(theLookupValue) match {
          case Found(index) => index
          case InsertionPoint(index) => index - 1
        }
        WithinBounds(values(indexToUse))
    }
  }
}

object BoundedBigDecimals {

  /**
   * Instantiate a BoundedBigDecimals.
   *
   * Sets the lower bound to the minimum value.
   *
   * Sets the upper bound to the supplied parameter. Typically, client code supplies a parameter greater than the
   * maximum value in "values". However, the only requirement is that the upper bound not be less than the lower bound.
   *
   * @param values The values to store. Must contain at least one value.
   * @param upperBound The upper bound.
   * @throws java.lang.IllegalArgumentException If "values" is empty, or if the upper bound is less than the lower
   *                                            bound.
   * @return The BoundedBigDecimals.
   */
  @throws(classOf[IllegalArgumentException])
  def apply(values: Set[BigDecimal], upperBound: BigDecimal): BoundedBigDecimals = {

    if (values.isEmpty) {
      throw new IllegalArgumentException("'values' must not be empty!")
    }

    val theValues = values.toIndexedSeq.sorted
    val lowerBound = theValues.min

    if (upperBound < lowerBound) {
      throw new IllegalArgumentException(s"Upper bound ($upperBound) is less than lower bound ($lowerBound)!")
    }

    new BoundedBigDecimals(theValues, lowerBound, upperBound)
  }
}

sealed trait ValueLessThanOrEqualToResult
case class WithinBounds(value: BigDecimal) extends ValueLessThanOrEqualToResult
case object GreaterThanUpperBound extends ValueLessThanOrEqualToResult
case object LessThanLowerBound extends ValueLessThanOrEqualToResult
