package com.dmanchester.approachminder.typeswithbehavior

import com.dmanchester.approachminder.utils.TrajectoryUtils
import com.dmanchester.approachminder.utils.TrajectoryUtils.positionsToSegments
import com.dmanchester.approachminder.GeographicCalculator
import com.dmanchester.approachminder.typeswithoutbehavior.HasLongLat

/**
 * An aircraft trajectory that continuously nears a reference point.
 *
 * The trajectory is available via both individual positions and two-position segments.
 *
 * Is guaranteed to contain at least two positions (one segment).
 *
 * NOTE: This class does not include the non-position identifiers found in a Trajectory (icao24, callsign, and
 * category), but if a need for them arose, it would be reasonable to add them.
 */
case class ContinuouslyNearingTrajectory[+P <: HasLongLat] private(positions: Seq[P], referencePoint: HasLongLat, calculator: GeographicCalculator) {
  val segments: Seq[(P, P)] = positionsToSegments(positions)
}

object ContinuouslyNearingTrajectory {

  /**
   * From a "regular" trajectory, create a ContinuouslyNearingTrajectory instance with the subsequence of segments that:
   *
   *   - includes a specified segment; and
   *   - continuously nears a reference point.
   *
   * @param trajectory The trajectory.
   * @param segmentIndex The 0-indexed segment that the subsequence of segments must include.
   * @param referencePoint The reference point.
   * @param calculator The GeographicCalculator to use for distance calculations.
   * @tparam P The positions' type.
   * @throws java.lang.IndexOutOfBoundsException if segmentIndex < 0 or segmentIndex > (segments.length - 1).
   * @return The ContinuouslyNearingTrajectory, along with the count of segments after the specified segment included
   *         within the trajectory, as a `Some`; or, `None` if the sequence's specified segment doesn't continuously
   *         near the reference point.
   */
  @throws(classOf[IndexOutOfBoundsException])
  def newOption[P <: HasLongLat](trajectory: Trajectory[P], segmentIndex: Int, referencePoint: HasLongLat, calculator: GeographicCalculator): Option[(ContinuouslyNearingTrajectory[P], Int)] = {

    // This method call also validates segmentIndex (and throws on an invalid value).
    val segmentsAtAndAfterIndex = TrajectoryUtils.continuouslyNearingSegmentsStartingAt(trajectory, segmentIndex, referencePoint, calculator)

    if (segmentsAtAndAfterIndex == 0) {
      None
    } else {

      val segmentsBeforeIndex = if (segmentIndex == 0) {
        0
      } else {
        TrajectoryUtils.continuouslyNearingSegmentsEndingAt(trajectory, segmentIndex - 1, referencePoint, calculator)
      }

      Some(new ContinuouslyNearingTrajectory(trajectory.positions.slice(segmentIndex - segmentsBeforeIndex, segmentIndex + segmentsAtAndAfterIndex + 1), referencePoint, calculator), segmentsAtAndAfterIndex - 1)
    }
  }
}
