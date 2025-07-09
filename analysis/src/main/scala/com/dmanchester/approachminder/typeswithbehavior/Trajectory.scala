package com.dmanchester.approachminder.typeswithbehavior

import com.dmanchester.approachminder.typeswithoutbehavior.AircraftCategory
import com.dmanchester.approachminder.utils.TrajectoryUtils.positionsToSegments

/**
 * A trajectory of an aircraft.
 *
 * The trajectory is available via both individual positions and two-position segments.
 *
 * Is guaranteed to contain at least two positions (one segment).
 */
case class Trajectory[+P] private(positions: Seq[P], icao24: String, callsign: Option[String], category: Option[AircraftCategory]) {

  val segments: Seq[(P, P)] = positionsToSegments(positions)

  /**
   * Whether trajectory's aircraft is possibly fixed-wing and powered.
   *
   * TODO Add tests.
   */
  def isPossiblyFixedWingPowered: Boolean = {
    // `Option.forAll` returns `true` on `None`.
    category.forall { theCategory =>
      AircraftCategory.fixedWingPowered.contains(theCategory)
    }
  }

  /**
   * Drop positions from the start of the trajectory.
   *
   * TODO Confirm how this method is used.
   * TODO Add tests.
   *
   * @param n The number of positions to drop.
   * @return The shortened trajectory, wrapped in `Some`; or `None`, if fewer than two positions remain.
   */
  def drop(n: Int): Option[Trajectory[P]] = Trajectory.newOption(positions.drop(n), icao24, callsign, category)

  /**
   * Whether a given index value in a valid lookup into the trajectory's positions: non-negative (and thus not "too
   * low"), and also not too high.
   *
   * TODO Confirm how this method is used.
   * TODO Add tests.
   */
  def isSegmentIndexValid(index: Int): Boolean = {
    index >= 0 && index <= (positions.length - 2)  // n positions constitute (n - 1) segments; with zero-based indexing, last segment's index is (n - 2)
  }

  /**
   * Truncate this trajectory *after* a certain position upon finding a subsequent position for which a predicate
   * evaluates true.
   *
   * @param positionIndex Must be between 1 and positions.length - 1, inclusive.
   * @param predicate The predicate.
   * @throws java.lang.IndexOutOfBoundsException If positionIndex is out of range.
   * @return The truncated trajectory; or, this trajectory, if no truncation was appropriate.
   */
  @throws(classOf[IndexOutOfBoundsException])
  def truncateWhere(positionIndex: Int, predicate: (P) => Boolean): Trajectory[P] = {

    if (positionIndex < 1 || positionIndex > positions.length - 1) {
      throw new IndexOutOfBoundsException(s"positionIndex is $positionIndex; must be between 1 and ${positions.length - 1}, inclusive!")
    }

    val positionToTruncateFrom = positions.indexWhere(predicate, positionIndex + 1)

    if (positionToTruncateFrom == -1) {
      this
    } else {
      new Trajectory(positions.take(positionToTruncateFrom), icao24, callsign, category)
    }
  }
}

object Trajectory {
  /**
   * Create a new trajectory, provided at least two positions are provided.
   */
  def newOption[P](positions: Seq[P], icao24: String, callsign: Option[String], category: Option[AircraftCategory]): Option[Trajectory[P]] = {
    Option.when(positions.length >= 2)(new Trajectory(positions, icao24, callsign, category))
  }
}
