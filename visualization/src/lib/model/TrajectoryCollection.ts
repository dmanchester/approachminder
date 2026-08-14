import { type Position } from "./Position";
import { type Trajectory } from "./Trajectory";

import { groupBy, maxBy, minBy } from "lodash";
import { JulianDate } from "cesium";

/**
 * An ordered collection of trajectories. Offers methods for querying across the trajectories.
 *
 * Is guaranteed to contain at least one trajectory.
 */
export class TrajectoryCollection {
  readonly trajectories: Array<Trajectory>;

  /**
   * Construct a TrajectoryCollection, retaining the trajectories' supplied order.
   *
   * @param trajectories The Trajectory instances to include. Must contain at least one.
   * @throws {Error} If trajectories is empty.
   */
  constructor(trajectories: Array<Trajectory>) {
    if (trajectories.length === 0) {
      throw new Error("trajectories must not be empty!");
    }

    this.trajectories = trajectories;
  }

  /**
   * The time of the earliest position of any trajectory.
   */
  earliestTime(): JulianDate {
    const earliestTimeOfEachTrajectory = this.trajectories.map((trajectory) =>
      trajectory.earliestTime(),
    );
    return minBy(earliestTimeOfEachTrajectory, (time) =>
      JulianDate.totalDays(time),
    )!; // totalDays includes whole and fractional days
  }

  /**
   * The time of the latest position of any trajectory.
   */
  latestTime(): JulianDate {
    const latestTimeOfEachTrajectory = this.trajectories.map((trajectory) =>
      trajectory.latestTime(),
    );
    return maxBy(latestTimeOfEachTrajectory, (time) =>
      JulianDate.totalDays(time),
    )!;
  }

  /**
   * Get the latest positions within a time window, one per aircraft.
   *
   * If an aircraft does not have a trajectory that intersects the time window, the aircraft is not included in the
   * result.
   *
   * If an aircraft has *multiple* trajectories that intersect the time window, the aircraft is included via its
   * trajectory having the latest position within the window.
   *
   * The start and end times of the window are considered "within" it.
   *
   * This method offers no guarantees around the sortedness of the returned array. It is acceptable for calling code to
   * sort it (and even expected), but it should *not* mutate elements of the array.
   *
   * @param endTime The end time of the window.
   * @param duration The duration of the window. (The start time is calculated using this duration.)
   */
  latestPositionsWithinWindow(
    endTime: JulianDate,
    duration: number,
  ): Array<Position> {
    // For each trajectory that intersects the time window, find the latest position within the window.
    const positionsUngrouped: Array<Position> = this.trajectories
      .map((trajectory) =>
        trajectory.latestPositionWithinWindow(endTime, duration),
      )
      .filter((position) => position !== undefined);

    // Group the found trajectories and positions by aircraft *physical identifier* (icao24).
    const positionsGroupedByAircraft: Record<string, Array<Position>> = groupBy(
      positionsUngrouped,
      (position) => position.trajectory.icao24,
    );

    // Obtain the latest position per aircraft.
    return Object.values(positionsGroupedByAircraft).map(
      (positionsOneAircraft) =>
        maxBy(positionsOneAircraft, (position) =>
          JulianDate.totalDays(position.time),
        )!,
    );
  }
}
