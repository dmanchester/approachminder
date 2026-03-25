import type AircraftProfile from "./AircraftProfile";
import Position from "./Position";
import type { PositionTemplate } from "./PositionTemplate";

import { JulianDate } from "cesium";

/**
 * A trajectory of an aircraft. Includes a time-ordered array of positions.
 *
 * The array is guaranteed to contain:
 *   * at least one position; and
 *   * no positions that share the same time.
 */
class Trajectory {
  readonly aircraftProfile: AircraftProfile;
  readonly positions: Array<Position>;

  /**
   * Construct a Trajectory.
   *
   * @param aircraftProfile The AircraftProfile to include.
   * @param positionTemplates A time-keyed Record/Object of PositionTemplate values to use in constructing the
   * Trajectory's Position instances. Must contain at least one entry, and the time must be in ISO-8601 format.
   * @throws {Error} If positionTemplates is empty.
   */
  constructor(aircraftProfile: AircraftProfile, positionTemplates: Record<string, PositionTemplate>) {

    if (Object.keys(positionTemplates).length === 0) {
      throw new Error("positionTemplates must not be empty!");
    }

    this.aircraftProfile = aircraftProfile;

    const positions = Object.entries(positionTemplates).map(([timeIso8601, template]) => {
      const time = JulianDate.fromIso8601(timeIso8601);
      return new Position(this /* Trajectory being constructed */, time, template);
    });

    positions.sort((a, b) => JulianDate.compare(a.time, b.time));

    this.positions = positions;
  }

  /**
   * The time of the earliest position.
   */
  earliestTime(): JulianDate {
    return this.positions[0].time;
  }

  /**
   * The time of the latest position.
   */
  latestTime(): JulianDate {
    return this.positions.at(-1)!.time;
  }

  /**
   * Get the latest position of this trajectory within a time window (if there are any positions within the window;
   * otherwise, return `undefined`).
   *
   * The start and end times of the window are considered "within" it.
   *
   * @param endTime The end time of the window.
   * @param duration The duration of the window. (The start time is calculated using this duration.)
   */
  latestPositionWithinWindow(endTime: JulianDate, duration: number): Position | undefined {
    const startTime = JulianDate.addSeconds(endTime, -1 * duration, new JulianDate());
    return this.positions.findLast(tbp => JulianDate.lessThanOrEquals(startTime, tbp.time) && JulianDate.lessThanOrEquals(tbp.time, endTime));
  }
}

export default Trajectory;
