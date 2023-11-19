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
   */
  earliestTime() {
    const earliestTimesOfTrajectories = this.theTrajectories.map(trajectory => trajectory.earliestTime());
    return minBy(earliestTimesOfTrajectories, (time) => JulianDate.totalDays(time));  // totalDays includes whole and fractional days
  }

  /**
   * TODO Document
   */
  latestTime() {
    const latestTimesOfTrajectories = this.theTrajectories.map(trajectory => trajectory.latestTime());
    return maxBy(latestTimesOfTrajectories, (time) => JulianDate.totalDays(time));
  }

  /**
   * Get the latest positions within a time window, one per aircraft.
   *
   * If an aircraft does not have a trajectory that intersects the time window, the aircraft is not
   * included in the output.
   *
   * The start and end times of the window are considered "within" it.
   *
   * @param {JulianDate} endTime
   * @param {Number} duration
   * @returns {Array<[AircraftProfile, TimeBasedPosition]>} Order within array is arbitrary -- TODO Note that using Array mostly because Set type is not very functional (e.g., no "has"/"contains" with predicate)
   */
  aircraftLatestPositionsWithinWindow(endTime, duration) {

    // For each trajectory that intersects the time window, find the latest position within the window.
    const latestPositionsWithinWindow = this.theTrajectories.map(trajectory => {
      return [trajectory.aircraftProfile, trajectory.latestPositionWithinWindow(endTime, duration)];
    }).filter(([aircraftProfile, tbp]) => {
      return tbp !== undefined;
    });

    // Group the found positions by aircraft *physical identifier* (icao24).
    const latestPositionsWithinWindowByAircraftWithKey = groupBy(latestPositionsWithinWindow, ([aircraftProfile, tbp]) => aircraftProfile.icao24);
    const latestPositionsWithinWindowByAircraft = Object.values(latestPositionsWithinWindowByAircraftWithKey);

    // Obtain the latest position per aircraft.
    return latestPositionsWithinWindowByAircraft.map(latestPositionsOneAircraft => maxBy(latestPositionsOneAircraft, ([aircraftProfile, tbp]) => JulianDate.totalDays(tbp.time)));
  }
}

export default Trajectories;
