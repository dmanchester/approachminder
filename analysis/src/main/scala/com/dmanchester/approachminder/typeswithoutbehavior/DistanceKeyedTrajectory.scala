package com.dmanchester.approachminder.typeswithoutbehavior

import com.dmanchester.approachminder.AngleAndAltitude

/**
 * A trajectory whose positions are keyed by distance.
 *
 * Positions are expressed as an angle relative to a reference point and an altitude. (The reference point is not stored
 * within the class, but if a need for the point arose, it would be reasonable to add it.)
 *
 * Maintains no independent ordering of positions. So, primarily suitable for representing trajectories where a
 * distance-based ordering is also implicitly the ordering of the positions; in other words, primarily suitable for a
 * continuously nearing trajectory, or a continuously distancing one.
 */
case class DistanceKeyedTrajectory(positions: Map[BigDecimal, AngleAndAltitude])
