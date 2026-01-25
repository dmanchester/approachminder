import type ApproachSegment from "./ApproachSegment"

import { JulianDate } from "cesium";

class TimeBasedPosition {
  constructor(
    public readonly time: JulianDate,
    public readonly longitude: number,
    public readonly latitude: number,
    public readonly altitude: number,
    public readonly onGround: boolean,
    public readonly velocity: number | null,
    public readonly trueTrack: number | null,
    public readonly verticalRate: number | null,
    public readonly squawk: string | null,
    public readonly approachSegment: ApproachSegment | null,
  ) {}
}

export default TimeBasedPosition;
