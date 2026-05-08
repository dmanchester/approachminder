import { Trajectory } from "../lib/Trajectory";

export function constructTrajectory(icao24: string, times: Array<string>): Trajectory {

  const positionTemplate = {
    longitude: 0,
    latitude: 0,
    altitude: 0,
    onGround: false,
    velocity: null,
    trueTrack: null,
    verticalRate: null,
    squawk: null,
    approachSegment: null,
  };

  const positionTemplates = times.map(time => [ time, positionTemplate ]);

  return new Trajectory(icao24, null, null, Object.fromEntries(positionTemplates));
}
