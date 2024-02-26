import IO from "../lib/IO.js";
import { Cartesian3, JulianDate } from "cesium";

describe("IO", function() {

  describe("trajectoriesFromParsedJSON()", function() {

    it("should correctly construct a Trajectories instance from parsed JSON", function() {

      const icao24A = "icao24A";
      const callsignA = "callsignA";
      const categoryA = "categoryA";

      const icao24B = "icao24B";
      const callsignB = "callsignB";
      const categoryB = "categoryB";

      const parsedJSON =
        [
          { icao24: icao24A,
            callsign: callsignA,
            category: categoryA,
            positions: {
              // Intentionally passing positions in non-chronological order.
              "2023-01-01T00:01:01Z": { longitude: 4.4, latitude: 5.5, altitude: 6.6, onGround: true, velocity: 4.4, trueTrack: 5.5, verticalRate: 6.6, squawk: "ABC" },
              "2023-01-01T00:00:01Z": { longitude: 1.1, latitude: 2.2, altitude: 3.3, onGround: true, velocity: 4.4, trueTrack: 5.5, verticalRate: 6.6, squawk: "ABC" }
            }
          },
          { icao24: icao24B,
            callsign: callsignB,
            category: categoryB,
            positions: {
              "2023-01-02T00:00:01Z": { longitude: 7.7, latitude: 8.8, altitude: 9.9, onGround: true, velocity: 4.4, trueTrack: 5.5, verticalRate: 6.6, squawk: "ABC" },
              "2023-01-02T00:01:01Z": { longitude: 10.0, latitude: 11.1, altitude: 12.2, onGround: true, velocity: 4.4, trueTrack: 5.5, verticalRate: 6.6, squawk: "ABC" }
            }
          }
        ];

      const trajectories = IO.trajectoriesFromParsedJSON(parsedJSON);

      expect(trajectories.theTrajectories.length).toEqual(2);

      const trajectoryA = trajectories.theTrajectories[0];

      expect(trajectoryA.aircraftProfile.icao24).toEqual(icao24A);
      expect(trajectoryA.aircraftProfile.callsign).toEqual(callsignA);
      expect(trajectoryA.aircraftProfile.category).toEqual(categoryA);

      expect(trajectoryA.timeBasedPositions.length).toEqual(2);
      expect(trajectoryA.timeBasedPositions[0].time.equals(JulianDate.fromIso8601("2023-01-01T00:00:01Z"))).toBeTrue();  // supplied second in parsedJSON, but is chronologically first
      expect(trajectoryA.timeBasedPositions[0].longitude).toEqual(1.1);
      expect(trajectoryA.timeBasedPositions[0].latitude).toEqual(2.2);
      expect(trajectoryA.timeBasedPositions[0].altitude).toEqual(3.3);
      expect(trajectoryA.timeBasedPositions[0].onGround).toEqual(true);
      expect(trajectoryA.timeBasedPositions[0].velocity).toEqual(4.4);
      expect(trajectoryA.timeBasedPositions[0].trueTrack).toEqual(5.5);
      expect(trajectoryA.timeBasedPositions[0].verticalRate).toEqual(6.6);
      expect(trajectoryA.timeBasedPositions[0].squawk).toEqual("ABC");
    });
  });
});
