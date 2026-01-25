import AircraftProfile from "./AircraftProfile";
import ApproachSegment from "./ApproachSegment";
import type { ParsedJSON } from "./ParsedJSON";
import TimeBasedPosition from "./TimeBasedPosition";
import Trajectories from "./Trajectories";
import Trajectory from "./Trajectory";

import type { JulianDate } from "cesium";

class IO {

  /**
   * Construct a `Trajectories` instance from parsed trajectories JSON.
   *
   * FIXME In a production build, if JulianDate.fromIso8601() is imported into this file and invoked in this function,
   * the resulting JulianDates are not recognized as such by SampledPositionProperty.addSamples() (invoked in
   * App.svelte). This leads to a stack trace with the message, "julianDate is required." -- As a workaround, we require
   * calling code to import JulianDate.fromIso8601() and pass it a reference.
   *
   * @param parsedJSON
   * @param julianDateFromIso8601
   */
  static trajectoriesFromParsedJSON(parsedJSON: ParsedJSON, julianDateFromIso8601: (iso8601String: string, result?: JulianDate) => JulianDate) {

    const theTrajectories = parsedJSON.map(trajectoryFromJSON => {

      const aircraftProfile = new AircraftProfile(trajectoryFromJSON.icao24, trajectoryFromJSON.callsign, trajectoryFromJSON.category);
      const timeBasedPositions = Object.entries(trajectoryFromJSON.positions).map(([timeFromJSON, positionFromJSON]) => {

        const approachSegmentFromJSON = positionFromJSON.approachSegment;
        const approachSegment = approachSegmentFromJSON ? new ApproachSegment(approachSegmentFromJSON.airport, approachSegmentFromJSON.threshold, approachSegmentFromJSON.thresholdDistanceMeters, approachSegmentFromJSON.verticalDevMeters, approachSegmentFromJSON.horizontalDevMeters, approachSegmentFromJSON.normalizedEuclideanDistance) : null;

        return new TimeBasedPosition(
            julianDateFromIso8601(timeFromJSON),
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
