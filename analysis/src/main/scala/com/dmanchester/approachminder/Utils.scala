package com.dmanchester.approachminder

import org.geotools.measure.Units

import scala.math.{pow, sin, sqrt, toRadians}

/**
 * Utility functions that operate on standard Scala datatypes.
 */
object Utils {

  /**
   * Whether `x` is between `a` and `b`, inclusive. Handles `a` <= `b` and `b` <= `a`, as well as
   * positive/negative/mixed `a` and `b`.
   *
   * @param x
   * @param a
   * @param b
   * @return
   */
  def isBetween(x: Double, a: Double, b: Double): Boolean = {

    (a <= x && x <= b) || (b <= x && x <= a)
  }

  /**
   * Interpolate the scalar value that is `percentage` of the distance from `a` to `b`.
   *
   * @param a
   * @param b
   * @param percentage
   * @return
   */
  def interpolateScalar(a: Double, b: Double, percentage: Double): Double = a + percentage * (b - a)

  /**
   * Calculate the length of an Isosceles triangle's base.
   *
   * @param apexAngleDegrees the triangle's apex angle, in degrees
   * @param legLength the length of the triangle's legs
   * @return The length. If the angle is negative, the length is, too.
   */
  def isoscelesBaseLength(apexAngleDegrees: Double, legLength: Double): Double = {

    val apexAngleRadians = toRadians(apexAngleDegrees)
    2.0 * legLength * sin(apexAngleRadians / 2.0)
  }

  def hypotenuseLength(aLength: Double, bLength: Double): Double = {
    sqrt(pow(aLength, 2) + pow(bLength, 2))
  }

  /**
   * Determine the most-common string in a collection.
   *
   * If multiple strings are equally common, pick the one that is alphabetically first (according to
   * the inherent ordering of `String`).
   *
   * @param strings
   * @return The most-common string, wrapped in `Some`. Or, if `strings` is empty, `None`.
   */
  def mostCommonString(strings: Iterable[String]): Option[String] = {
    Utils.mostCommonValue(strings) { (a, b) => a < b }
  }

  /**
   * Determine the most-common value in a collection.
   *
   * If multiple values are equally common (i.e., they have the same number of occurrences), pick
   * the one that comes first according to a user-supplied discriminator function.
   *
   * @param values
   * @param leftComesFirst Discriminator function. Given `left` and `right`, returns whether `left` comes first.
   * @tparam T
   * @return The most-common value, wrapped in `Some`. Or, if `values` is empty, `None`.
   */
  def mostCommonValue[T](values: Iterable[T])(leftComesFirst: (T, T) => Boolean): Option[T] = {

    Option.when(values.nonEmpty) {

      val valuesAndCounts = values.groupBy(identity).map { case (value, valueOccurrences) =>
        (value, valueOccurrences.size)
      }.toSeq

      // Pick value and count "left" if:
      //
      //   * left's count is higher than right's; or,
      //   * their counts are the same, but "left" comes first, according to the discriminator function.
      //
      // Otherwise, pick "right".
      val mostCommonValueWithCount = valuesAndCounts.reduce { (left, right) =>
        if (left._2 > right._2 || (left._2 == right._2 && leftComesFirst(left._1, right._1)))
          left
        else
          right
      }

      mostCommonValueWithCount._1
    }
  }

  /**
   * Determine the most-common value in a collection. Collection cannot be empty.
   *
   * Differs from `mostCommonValue` only in that it imposes that non-empty requirement, and in that it returns `T` (not
   * `Option[T]`).
   *
   * @param values
   * @param leftComesFirst
   * @tparam T
   * @throws java.lang.UnsupportedOperationException If collection is empty.
   * @return
   */
  @throws(classOf[UnsupportedOperationException])
   def mostCommonValueInNonEmptyCollection[T](values: Iterable[T])(leftComesFirst: (T, T) => Boolean): T = {
    mostCommonValue(values)(leftComesFirst).get
  }

  val feetToMetersConverter = Units.FOOT.getConverterTo(Units.METRE)
}
