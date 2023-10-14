import IO from "../lib/IO.mjs";
import { Cartesian3, JulianDate } from "cesium";

describe("IO", function() {

  describe("trajectoriesFromParsedJSON()", function() {

    it("should correctly construct a Trajectories instance from parsed JSON", function() {

      const parsedJSON =
        [
          { icao24: "abc123",
            positions: {
              // Intentionally passing positions in non-chronological order.
              "2023-01-01T00:01:01Z": { longitude: 4.4, latitude: 5.5, altitude: 6.6 },
              "2023-01-01T00:00:01Z": { longitude: 1.1, latitude: 2.2, altitude: 3.3 }
            }
          },
          { icao24: "def456",
            positions: {
              "2023-01-02T00:00:01Z": { longitude: 7.7, latitude: 8.8, altitude: 9.9 },
              "2023-01-02T00:01:01Z": { longitude: 10.0, latitude: 11.1, altitude: 12.2 }
            }
          }
        ];

      const trajectories = IO.trajectoriesFromParsedJSON(parsedJSON);

      expect(trajectories.theTrajectories.length).toEqual(2);

      const trajectory0 = trajectories.theTrajectories[0];

      expect(trajectory0.aircraftProfile.icao24).toEqual("abc123");

      expect(trajectory0.timeBasedPositions.length).toEqual(2);
      expect(trajectory0.timeBasedPositions[0].time.equals(JulianDate.fromIso8601("2023-01-01T00:00:01Z"))).toBeTrue();  // supplied second in parsedJSON, but is chronologically first
      expect(trajectory0.timeBasedPositions[0].position.equals(Cartesian3.fromDegrees(1.1, 2.2, 3.3))).toBeTrue();
    });
  });
});
