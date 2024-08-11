package com.dmanchester.approachminder

/**
 * A trajectory whose positions are keyed by distance. Guaranteed to include at least one position.
 *
 * Positions are expressed as a heading and an altitude. (TODO Do I use "heading" terminology elsewhere?)
 *
 * Maintains no independent ordering of positions. So, primarily suitable for representing trajectories where a
 * distance-based ordering is also implicitly the ordering of the positions; in other words, primarily suitable for a
 * continuously nearing trajectory, or a continuously distancing one.
 *
 * @param positions
 */
case class DistanceKeyed3DTrajectory(positions: Map[BigDecimal, AngleAndAltitude])

object DistanceKeyed3DTrajectory {

  // TODO Suppress default "apply"

  def newOption(positions: Map[BigDecimal, AngleAndAltitude]): Option[DistanceKeyed3DTrajectory] = {
    Option.when(positions.nonEmpty) {
      new DistanceKeyed3DTrajectory(positions)
    }
  }
}
