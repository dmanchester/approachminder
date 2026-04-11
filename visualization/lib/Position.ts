import type ApproachSegment from "./ApproachSegment";
import type { PositionTemplate } from "./PositionTemplate";
import Trajectory from "./Trajectory";

import { JulianDate } from "cesium";

/**
 * A position of an aircraft at a point in time. Includes a reference to the Trajectory to which it belongs.
 *
 * If the position has been deemed part of an approach to a runway, also includes an ApproachSegment.
 */
class Position {
  readonly trajectory: Trajectory;
  readonly time: JulianDate;
  readonly longitude: number;
  readonly latitude: number;
  readonly altitude: number;
  readonly onGround: boolean;
  readonly velocity: number | null;
  readonly trueTrack: number | null;
  readonly verticalRate: number | null;
  readonly squawk: string | null;
  readonly approachSegment: ApproachSegment | null;

  constructor(trajectory: Trajectory, time: JulianDate, template: PositionTemplate) {
    this.trajectory = trajectory;
    this.time = time;
    this.longitude = template.longitude;
    this.latitude = template.latitude;
    this.altitude = template.altitude;
    this.onGround = template.onGround;
    this.velocity = template.velocity;
    this.trueTrack = template.trueTrack;
    this.verticalRate = template.verticalRate;
    this.squawk = template.squawk;
    this.approachSegment = template.approachSegment;
  }
}

export default Position;
