package com.dmanchester.approachminder.typeswithoutbehavior

/**
 * A polygon.
 *
 * @param perimeter The points forming the polygon's perimeter. The expectation is that the perimeter will not be closed
 *                  (i.e., that the last point won't repeat the first); that will be done as necessary when class
 *                  instances are used.
 */
case class Polygon(perimeter: Seq[HasLongLat])
