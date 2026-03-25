import AircraftProfile from "../lib/AircraftProfile";
import Trajectory from "../lib/Trajectory";

export function constructTrajectory(icao24: string, times: Array<string>): Trajectory {

  const aircraftProfile = new AircraftProfile(icao24, null, null);

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

  return new Trajectory(aircraftProfile, Object.fromEntries(positionTemplates));
}
