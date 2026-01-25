import type TimeBasedPosition from "./TimeBasedPosition";
import type Trajectory from "./Trajectory";

import { groupBy, maxBy, minBy } from "lodash";
import { JulianDate } from "cesium";

class Trajectories {

  /**
   * TODO Enforce at least one element in the Array<Trajectory>? Somewhat on the hook to do so, based on using "!" below
   * on array operations.
   *
   * @param theTrajectories
   */
  constructor(
    public readonly theTrajectories: Array<Trajectory>
  ) {}

  /**
   * TODO Document
   */
  earliestTime(): JulianDate {
    const earliestTimesOfTrajectories = this.theTrajectories.map(trajectory => trajectory.earliestTime());
    return minBy(earliestTimesOfTrajectories, time => JulianDate.totalDays(time))!;  // totalDays includes whole and fractional days
  }

  /**
   * TODO Document
   */
  latestTime(): JulianDate {
    const latestTimesOfTrajectories = this.theTrajectories.map(trajectory => trajectory.latestTime());
    return maxBy(latestTimesOfTrajectories, time => JulianDate.totalDays(time))!;
  }

  /**
   * Get the latest positions within a time window, one per aircraft.
   *
   * If an aircraft does not have a trajectory that intersects the time window, the aircraft is not included in the
   * output.
   *
   * If an aircraft has *multiple* trajectories that intersect the time window, the aircraft is included via its
   * trajectory having the latest position within the window.
   *
   * The start and end times of the window are considered "within" it.
   *
   * It is acceptable for calling code to sort the returned array, but it should *not* mutate elements of the array.
   *
   * TODO Can we/should we offer any guarantees about sortedness? (Probably not, based on my TODO about Set.)
   * 
   * TODO Note that using Array return mostly because Set type is not very functional (e.g., no "has"/"contains" with predicate)
   *
   * @param endTime
   * @param duration
   */
  latestPositionsWithinWindow(endTime: JulianDate, duration: number): Array<[Trajectory, TimeBasedPosition]> {

    // Throughout this method, "latest" in a variable name indicates latest *within the time window* passed to the
    // method. ("WithinWindow" is omitted from names for brevity.)

    // For each trajectory that intersects the time window, find the latest position within the window.
    const trajectoriesAndLatestPositions = this.theTrajectories.map(trajectory => {
      return [trajectory, trajectory.latestPositionWithinWindow(endTime, duration)] as [Trajectory, TimeBasedPosition];
    }).filter(([_trajectory, timeBasedPosition]) => {
      return timeBasedPosition !== undefined;
    });

    // Group the found trajectories and positions by aircraft *physical identifier* (icao24).
    const trajectoriesAndLatestPositionsGroupedByAircraft = Object.values(
      groupBy(
        trajectoriesAndLatestPositions,
        ([trajectory, _timeBasedPosition]) => trajectory.aircraftProfile.icao24
      )
    );

    // Obtain the latest position per aircraft.
    return trajectoriesAndLatestPositionsGroupedByAircraft.map(trajectoriesAndLatestPositionsOneAircraft =>
      maxBy(trajectoriesAndLatestPositionsOneAircraft, ([_trajectory, timeBasedPosition]) => JulianDate.totalDays(timeBasedPosition.time))!
    );
  }
}

export default Trajectories;
