import AircraftProfile from "./AircraftProfile";
import ApproachSegment from "./ApproachSegment";
import type { ParsedJSON } from "./ParsedJSON";
import TimeBasedPosition from "./TimeBasedPosition";
import Trajectories from "./Trajectories";
import Trajectory from "./Trajectory";

import { JulianDate } from "cesium";

// TODO Does it add value to have these functions in a namespace? If not, eliminate it.
namespace IO {

  /**
   * Construct a `Trajectories` instance from parsed trajectories JSON.
   *
   * @param parsedJSON
   */
  export function trajectoriesFromParsedJSON(parsedJSON: ParsedJSON) {

    const theTrajectories = parsedJSON.map(trajectoryFromJSON => {

      const aircraftProfile = new AircraftProfile(trajectoryFromJSON.icao24, trajectoryFromJSON.callsign, trajectoryFromJSON.category);
      const timeBasedPositions = Object.entries(trajectoryFromJSON.positions).map(([timeFromJSON, positionFromJSON]) => {

        const approachSegmentFromJSON = positionFromJSON.approachSegment;
        const approachSegment = approachSegmentFromJSON ? new ApproachSegment(approachSegmentFromJSON.airport, approachSegmentFromJSON.threshold, approachSegmentFromJSON.thresholdDistanceMeters, approachSegmentFromJSON.verticalDevMeters, approachSegmentFromJSON.horizontalDevMeters, approachSegmentFromJSON.normalizedEuclideanDistance) : null;

        return new TimeBasedPosition(
          JulianDate.fromIso8601(timeFromJSON),
          positionFromJSON.longitude,
          positionFromJSON.latitude,
          positionFromJSON.altitude,
          positionFromJSON.onGround,
          positionFromJSON.velocity,
          positionFromJSON.trueTrack,
          positionFromJSON.verticalRate,
          positionFromJSON.squawk,
          approachSegment
        );
      });

      return new Trajectory(aircraftProfile, timeBasedPositions);
    });

    return new Trajectories(theTrajectories);
  }
}

export default IO;
