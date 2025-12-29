package com.dmanchester.approachminder.typeswithbehavior

import com.dmanchester.approachminder.utils.{GeographicCalculator, TrajectoryUtils}
import com.dmanchester.approachminder.utils.TrajectoryUtils.positionsToSegments
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
   * From a source Trajectory, create a ContinuouslyNearingTrajectory with the subsequence of source-trajectory segments
   * that:
   *
   *   - includes a specified segment; and
   *   - continuously nears a reference point.
   *
   * @param sourceTrajectory The source trajectory.
   * @param segmentIndexSourceTrajectory The segment in sourceTrajectory that the ContinuouslyNearingTrajectory must
   *                                     include.
   * @param referencePoint The reference point.
   * @param calculator The GeographicCalculator to use for distance calculations.
   * @tparam P The type of sourceTrajectory's positions.
   * @throws java.lang.IndexOutOfBoundsException If segmentIndexSourceTrajectory < 0 or
   *                                             segmentIndexSourceTrajectory > (sourceTrajectory.segments.length - 1).
   * @return The ContinuouslyNearingTrajectory and the index of the specified segment within the
   *         ContinuouslyNearingTrajectory, wrapped in Some; or, None if the sourceTrajectory's specified segment
   *         doesn't continuously near the reference point. -- The returned index is a counterpart of the
   *         segmentIndexSourceTrajectory passed to this method. Both values refer to the same segment;
   *         segmentIndexSourceTrajectory does so in the context of sourceTrajectory, the returned index does so in the
   *         context of the ContinuouslyNearingTrajectory.
   */
  @throws(classOf[IndexOutOfBoundsException])
  def newOption[P <: HasLongLat](sourceTrajectory: Trajectory[P], segmentIndexSourceTrajectory: Int, referencePoint: HasLongLat, calculator: GeographicCalculator): Option[(ContinuouslyNearingTrajectory[P], Int)] = {

    // This method call also validates segmentIndexSourceTrajectory (and throws on an invalid value).
    val segmentsAtAndAfterIndex = TrajectoryUtils.continuouslyNearingSegmentsStartingAt(sourceTrajectory, segmentIndexSourceTrajectory, referencePoint, calculator)

    if (segmentsAtAndAfterIndex == 0) {
      None
    } else {

      val segmentsBeforeIndex = if (segmentIndexSourceTrajectory == 0) {
        0
      } else {
        TrajectoryUtils.continuouslyNearingSegmentsEndingAt(sourceTrajectory, segmentIndexSourceTrajectory - 1, referencePoint, calculator)
      }

      Some(new ContinuouslyNearingTrajectory(sourceTrajectory.positions.slice(segmentIndexSourceTrajectory - segmentsBeforeIndex, segmentIndexSourceTrajectory + segmentsAtAndAfterIndex + 1), referencePoint, calculator), segmentsBeforeIndex)
    }
  }
}
