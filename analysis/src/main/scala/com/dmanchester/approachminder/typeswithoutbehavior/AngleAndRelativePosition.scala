package com.dmanchester.approachminder.typeswithoutbehavior

import com.dmanchester.approachminder.typeswithbehavior.PolarAngle

/**
 * A polar angle and a relative position.
 *
 * Instances of this class are typically used to represent a position on a line segment relative to a reference point.
 *
 * @param angle The position's polar angle, relative to the reference point (i.e., with the reference point serving as
 *              an origin).
 * @param relativePosition Where the position lies along the line segment, expressed as a percentage from the segment's
 *                         first point.
 */
case class AngleAndRelativePosition(angle: PolarAngle, relativePosition: Double)
