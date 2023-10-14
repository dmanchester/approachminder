import AircraftProfile from "./AircraftProfile.mjs";
import TimeBasedPosition from "./TimeBasedPosition.mjs";
import Trajectories from "./Trajectories.mjs";
import Trajectory from "./Trajectory.mjs";
import { Cartesian3, JulianDate } from "cesium";

class IO {

  /**
   * Construct a `Trajectories` instance from parsed trajectories JSON.
   *
   * @param {Object} parsedJSON
   */
  static trajectoriesFromParsedJSON(parsedJSON) {

    const theTrajectories = parsedJSON.map((trajectoryFromJSON) => {

      const aircraftProfile = new AircraftProfile(trajectoryFromJSON.icao24);
      const timeBasedPositions = Object.entries(trajectoryFromJSON.positions).map(([timeFromJSON, positionFromJSON]) => {
        return new TimeBasedPosition(JulianDate.fromIso8601(timeFromJSON), Cartesian3.fromDegrees(positionFromJSON.longitude, positionFromJSON.latitude, positionFromJSON.altitude));
      });

      return new Trajectory(aircraftProfile, timeBasedPositions);
    });

    return new Trajectories(theTrajectories);
  }
}

export default IO;
