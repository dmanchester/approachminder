import { JulianDate } from "cesium";
import uniqWith from "lodash/uniqWith.js";

class Trajectory {

  /**
   * Construct an instance.
   TODO Enforce at least one TBP?
   * @param {AircraftProfile} aircraftProfile
   * @param {Array<TimeBasedPosition>} timeBasedPositions
   */
  constructor(aircraftProfile, timeBasedPositions) {
    this.aircraftProfile = aircraftProfile;
    this.timeBasedPositions = Trajectory.tidyTimeBasedPositions(timeBasedPositions);
  }

  /**
   *
   * @param {Array<TimeBasedPosition>} untidy
   * @returns {Array<TimeBasedPosition>}
   */
  static tidyTimeBasedPositions(untidy) {  // TODO Is there a concept of marking private?

    const tidyInProgress = untidy.slice();  // shallow copy

    tidyInProgress.sort((a, b) => JulianDate.compare(a.time, b.time));
    return uniqWith(tidyInProgress, (a, b) => JulianDate.equals(a.time, b.time));
  }

  /**
   * TODO Document
   * @returns {JulianDate}
   */
  earliestTime() {
    return this.timeBasedPositions[0].time;
  }

  /**
   * TODO Document
   * @returns {JulianDate}
   */
  latestTime() {
    return this.timeBasedPositions.at(-1).time;
  }

  /**
   * Get the latest position of this trajectory within a time window (if there are any positions
   * within the window; otherwise, return `undefined`).
   *
   * The start and end times of the window are considered "within" it.
   *
   * @param {JulianDate} endTime
   * @param {Number} duration
   * @returns {TimeBasedPosition}
   */
  latestPositionWithinWindow(endTime, duration) {
    const startTime = JulianDate.addSeconds(endTime, -1 * duration, new JulianDate());
    return this.timeBasedPositions.findLast(tbp => JulianDate.lessThanOrEquals(startTime, tbp.time) && JulianDate.lessThanOrEquals(tbp.time, endTime));
  }
}

export default Trajectory;
