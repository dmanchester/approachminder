package com.dmanchester.approachminder

/**
 * A polygon.
 *
 * @param perimeter The points forming the polygon's perimeter. The expectation is that the
 *                  perimeter will not be closed (i.e., that the last point won't repeat the first);
 *                  that will be done as necessary when class instances are used.
 */
class Polygon private(val perimeter: Seq[HasLongLat])

object Polygon {
  def apply(perimeter: Seq[HasLongLat]): Polygon = new Polygon(perimeter)
}
