import type AircraftProfile from "./AircraftProfile";
import type TimeBasedPosition from "./TimeBasedPosition";

import { JulianDate } from "cesium";
import { uniqWith } from "lodash";

class Trajectory {
  readonly aircraftProfile: AircraftProfile;
  readonly timeBasedPositions: Array<TimeBasedPosition>;

  /**
   * TODO Enforce at least one element in the Array<TimeBasedPosition>? Somewhat on the hook to do so, based on using
   * "!" below on array operations.
   *
   * @param aircraftProfile
   * @param timeBasedPositions
   */
  constructor(aircraftProfile: AircraftProfile, timeBasedPositions: Array<TimeBasedPosition>) {
    this.aircraftProfile = aircraftProfile;
    this.timeBasedPositions = Trajectory.tidyTimeBasedPositions(timeBasedPositions);
  }

  private static tidyTimeBasedPositions(untidy: Array<TimeBasedPosition>): Array<TimeBasedPosition> {

    const tidyInProgress = untidy.slice();  // shallow copy

    tidyInProgress.sort((a, b) => JulianDate.compare(a.time, b.time));
    return uniqWith(tidyInProgress, (a, b) => JulianDate.equals(a.time, b.time));
  }

  /**
   * TODO Document
   */
  earliestTime(): JulianDate {
    return this.timeBasedPositions[0].time;
  }

  /**
   * TODO Document
   */
  latestTime(): JulianDate {
    return this.timeBasedPositions.at(-1)!.time;
  }

  /**
   * Get the latest position of this trajectory within a time window (if there are any positions within the window;
   * otherwise, return `undefined`).
   *
   * The start and end times of the window are considered "within" it.
   *
   * @param endTime
   * @param duration
   */
  latestPositionWithinWindow(endTime: JulianDate, duration: number): TimeBasedPosition {
    const startTime = JulianDate.addSeconds(endTime, -1 * duration, new JulianDate());
    return this.timeBasedPositions.findLast(tbp => JulianDate.lessThanOrEquals(startTime, tbp.time) && JulianDate.lessThanOrEquals(tbp.time, endTime))!;
  }
}

export default Trajectory;
