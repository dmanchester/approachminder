import { Position } from "./Position";
import { type PositionTemplate } from "./PositionTemplate";

import { JulianDate } from "cesium";

/**
 * A trajectory of an aircraft. Includes a time-ordered array of positions.
 *
 * The array is guaranteed to contain:
 *   * at least one position; and
 *   * no positions that share the same time.
 */
export class Trajectory {
  readonly icao24: string;
  readonly callsign: string | null;
  readonly category: string | null;
  readonly positions: Array<Position>;

  /**
   * Construct a Trajectory.
   *
   * (For more information on the "icao24", "callsign", and "category" parameters below, see
   * https://openskynetwork.github.io/opensky-api/rest.html.)
   *
   * @param icao24 The 24-bit ICAO address (in hex form) of the transponder of the aircraft that flew the trajectory.
   * @param callsign The callsign the aircraft used.
   * @param category The aircraft's category. Is a simple toString() of the Scala AircraftCategory class, so values have
   * a trailing dollar sign (e.g., "Heavy$", "Small$").
   * @param positionTemplates A time-keyed Record/Object of PositionTemplate values to use in constructing the
   * Trajectory's Position instances. Must contain at least one entry, and the time must be in ISO-8601 format.
   * @throws {Error} If positionTemplates is empty.
   */
  constructor(icao24: string, callsign: string | null, category: string | null, positionTemplates: Record<string, PositionTemplate>) {

    if (Object.keys(positionTemplates).length === 0) {
      throw new Error("positionTemplates must not be empty!");
    }

    this.icao24 = icao24;
    this.callsign = callsign;
    this.category = category;

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
