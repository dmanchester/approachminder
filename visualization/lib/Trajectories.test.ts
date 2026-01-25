import IO from "./IO";

import { expect, test } from "vitest";
import { JulianDate } from "cesium";

const parsedJSON =
  [
    { icao24: "def456",
      positions: {
        "2023-01-02T00:00:01Z": { longitude: 7.7, latitude: 8.8, altitude: 9.9 },
        "2023-01-02T00:01:01Z": { longitude: 10.0, latitude: 11.1, altitude: 12.2 }
      }
    },
    { icao24: "abc123",
      positions: {
        "2023-01-01T00:00:01Z": { longitude: 1.1, latitude: 2.2, altitude: 3.3 },
        "2023-01-01T00:01:01Z": { longitude: 4.4, latitude: 5.5, altitude: 6.6 }
      }
    },
    { icao24: "ghi789",
      positions: {
        "2023-01-01T00:02:01Z": { longitude: 13.3, latitude: 14.4, altitude: 15.5 },
        "2023-01-01T00:03:01Z": { longitude: 16.6, latitude: 17.7, altitude: 18.8 }
      }
    },
    { icao24: "ghi789",
      positions: {
        "2023-01-01T00:04:01Z": { longitude: 19.9, latitude: 20.0, altitude: 21.1 },
        "2023-01-01T00:05:01Z": { longitude: 22.2, latitude: 23.3, altitude: 24.4 },
        "2023-01-02T00:02:01Z": { longitude: 25.5, latitude: 26.6, altitude: 27.7 }
      }
    },
    { icao24: "jkl012",
      positions: {
        "2023-01-03T00:00:01Z": { longitude: 28.8, latitude: 29.9, altitude: 30.0 },
        "2023-01-03T00:01:01Z": { longitude: 31.1, latitude: 32.2, altitude: 33.3 }
      }
    }
  ];

const trajectories = IO.trajectoriesFromParsedJSON(parsedJSON as any, JulianDate.fromIso8601);

test("earliestTime() should give the earliest time of any trajectory", () => {
  const expectedDate = JulianDate.fromIso8601("2023-01-01T00:00:01Z");
  expect(trajectories.earliestTime().equals(expectedDate)).toEqual(true);
});

test("latestTime() should give the latest time of any trajectory", () => {
  const expectedDate = JulianDate.fromIso8601("2023-01-03T00:01:01Z");
  expect(trajectories.latestTime().equals(expectedDate)).toEqual(true);
});

test("latestPositionsWithinWindow() should work per its documentation", () => {  // TODO Write a better description
  const latestPositions = trajectories.latestPositionsWithinWindow(JulianDate.fromIso8601("2023-01-02T00:00:01Z"), 23 * 60 * 60 + 59 * 60);  // 23 hours, 59 minutes: 2023-01-01T00:01:01Z to 2023-01-02T00:00:01Z

  expect(latestPositions.length).toEqual(3);  // no position from jkl012; its trajectory later than window

  expect(latestPositions.some(([trajectory, timeBasedPosition]) => trajectory.aircraftProfile.icao24 === "abc123" && timeBasedPosition.time.equals(JulianDate.fromIso8601("2023-01-01T00:01:01Z")))).toEqual(true);
  expect(latestPositions.some(([trajectory, timeBasedPosition]) => trajectory.aircraftProfile.icao24 === "def456" && timeBasedPosition.time.equals(JulianDate.fromIso8601("2023-01-02T00:00:01Z")))).toEqual(true);
  expect(latestPositions.some(([trajectory, timeBasedPosition]) => trajectory.aircraftProfile.icao24 === "ghi789" && timeBasedPosition.time.equals(JulianDate.fromIso8601("2023-01-01T00:05:01Z")))).toEqual(true);
});
