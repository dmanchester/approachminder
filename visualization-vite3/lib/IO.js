import AircraftProfile from "./AircraftProfile.js";
import TimeBasedPosition from "./TimeBasedPosition.js";
import Trajectories from "./Trajectories.js";
import Trajectory from "./Trajectory.js";
import { Cartesian3, JulianDate } from "cesium";
import ApproachSegment from "./ApproachSegment.js";

class IO {

  /**
   * Construct a `Trajectories` instance from parsed trajectories JSON.
   *
   * @param {object} parsedJSON
   * @returns {Trajectories}
   */
  static trajectoriesFromParsedJSON(parsedJSON) {

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
