import { JulianDate } from "cesium";
import groupBy from "lodash/groupBy.js";
import maxBy from "lodash/maxBy.js";
import minBy from "lodash/minBy.js";

class Trajectories {

  /**
   * Construct an instance.
   * @param {Array<Trajectory>} theTrajectories
   */
  constructor(theTrajectories) {
    this.theTrajectories = theTrajectories;
  }

  /**
   * TODO Document
   * @returns {JulianDate}
   */
  earliestTime() {
    const earliestTimesOfTrajectories = this.theTrajectories.map(trajectory => trajectory.earliestTime());
    return minBy(earliestTimesOfTrajectories, (time) => JulianDate.totalDays(time));  // totalDays includes whole and fractional days
  }

  /**
   * TODO Document
   * @returns {JulianDate}
   */
  latestTime() {
    const latestTimesOfTrajectories = this.theTrajectories.map(trajectory => trajectory.latestTime());
    return maxBy(latestTimesOfTrajectories, (time) => JulianDate.totalDays(time));
  }

  /**
   * Get the latest position per aircraft within a time window.
   *
   * If an aircraft does not have a trajectory that intersects the time window, the aircraft is not
   * included in the output.
   *
   * If an aircraft has *multiple* trajectories that intersect the time window, the aircraft is
   * included via its trajectory having the latest position within the window.
   *
   * The start and end times of the window are considered "within" it.
   *
   * It is acceptable for calling code to sort the returned array, but it should *not* mutate
   * elements of the array.
   *
   * TODO Can we/should we offer any guarantees about sortedness? (Probably not, based on my TODO about Set.)
   * 
   * @param {JulianDate} endTime
   * @param {Number} duration
   * TODO Note that using Array return mostly because Set type is not very functional (e.g., no "has"/"contains" with predicate)
   * @returns {Array<[Trajectory, TimeBasedPosition]>}
   */
  latestPositionWithinWindowByAircraft(endTime, duration) {

    // For each trajectory that intersects the time window, find the latest position within the window.
    const latestPositionWithinWindowByTrajectory = this.theTrajectories.map(trajectory => {
      return [trajectory, trajectory.latestPositionWithinWindow(endTime, duration)];
    }).filter(([trajectory, timeBasedPosition]) => {
      return timeBasedPosition !== undefined;
    });

    // Group the found positions by aircraft *physical identifier* (icao24).
    const latestPositionsWithinWindowByAircraftWithKey =
        groupBy(
            latestPositionWithinWindowByTrajectory,
            ([trajectory, timeBasedPosition]) => trajectory.aircraftProfile.icao24
        );
    const latestPositionsWithinWindowByAircraft = Object.values(latestPositionsWithinWindowByAircraftWithKey);

    // Obtain the latest position per aircraft.
    return latestPositionsWithinWindowByAircraft.map(latestPositionsOneAircraft =>
        maxBy(latestPositionsOneAircraft, ([trajectory, timeBasedPosition]) => JulianDate.totalDays(timeBasedPosition.time))
    );
  }
}

export default Trajectories;
