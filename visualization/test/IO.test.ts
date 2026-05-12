import { constructTrajectoryCollection } from "../lib/IO";
import { TrajectoryCollectionTemplate } from "../lib/TrajectoryCollectionTemplate";

import { JulianDate } from "cesium";

import { describe, expect, test } from "vitest";

describe("constructTrajectoryCollection()", () => {

  const icao24A = "icao24A";
  const callsignA = "callsignA";
  const categoryA = "categoryA";
  const timeA0 = "2023-01-01T00:00:01Z";
  const timeA1 = "2023-01-01T00:01:01Z";

  const icao24B = "icao24B";
  const callsignB = "callsignB";
  const categoryB = "categoryB";

  const template: TrajectoryCollectionTemplate = [
    {
      icao24: icao24A,
      callsign: callsignA,
      category: categoryA,
      positions: {
        // Intentionally passing positions in non-chronological order.
        [timeA1]: {
          longitude: 7.7, latitude: 8.8, altitude: 9.9, onGround: false, velocity: 1.2, trueTrack: 3.4, verticalRate: 5.6, squawk: "DEF", approachSegment: {
            airport: "GHI", threshold: "JKL", thresholdDistanceMeters: 7.8, verticalDevMeters: 9.1, horizontalDevMeters: 2.3, normalizedEuclideanDistance: 4.5
          }
        },
        [timeA0]: { longitude: 1.1, latitude: 2.2, altitude: 3.3, onGround: true, velocity: 4.4, trueTrack: 5.5, verticalRate: 6.6, squawk: "ABC", approachSegment: null }
      }
    },
    {
      icao24: icao24B,
      callsign: callsignB,
      category: categoryB,
      positions: {
        "2023-01-02T00:00:01Z": { longitude: 7.7, latitude: 8.8, altitude: 9.9, onGround: true, velocity: 4.4, trueTrack: 5.5, verticalRate: 6.6, squawk: "ABC", approachSegment: null }
      }
    }
  ];

  const trajectoryCollection = constructTrajectoryCollection(template);

  const trajectoryA = trajectoryCollection.trajectories[0];
  const trajectoryB = trajectoryCollection.trajectories[1];

  test("should construct a collection of the correct size and with trajectories ordered as in the template", () => {
    expect(trajectoryCollection.trajectories.length).toBe(2);
    expect(trajectoryA.icao24).toBe(icao24A);
    expect(trajectoryB.icao24).toBe(icao24B);
  });

  test("should construct a collection with top-level trajectory properties set correctly and with trajectories' positions ordered correctly", () => {
    expect(trajectoryA.callsign).toBe(callsignA);
    expect(trajectoryA.category).toBe(categoryA);
    expect(trajectoryA.positions[0].time.equals(JulianDate.fromIso8601(timeA0))).toBe(true);
    expect(trajectoryA.positions[1].time.equals(JulianDate.fromIso8601(timeA1))).toBe(true);
  });

  test("should, within a collection, construct a trajectory position correctly that lacks an approach segment", () => {
    expect(trajectoryA.positions[0].longitude).toBe(1.1);
    expect(trajectoryA.positions[0].latitude).toBe(2.2);
    expect(trajectoryA.positions[0].altitude).toBe(3.3);
    expect(trajectoryA.positions[0].onGround).toBe(true);
    expect(trajectoryA.positions[0].velocity).toBe(4.4);
    expect(trajectoryA.positions[0].trueTrack).toBe(5.5);
    expect(trajectoryA.positions[0].verticalRate).toBe(6.6);
    expect(trajectoryA.positions[0].squawk).toBe("ABC");
  });

  test("should, within a collection, construct a trajectory position correctly that has an approach segment", () => {
    // We don't generally bother re-testing the properties that were tested by the previous test. The exception is the
    // boolean ("onGround"). The previous test checked "true"; this one checks "false".
    expect(trajectoryA.positions[1].onGround).toBe(false);
    expect(trajectoryA.positions[1].approachSegment.airport).toBe("GHI");
    expect(trajectoryA.positions[1].approachSegment.threshold).toBe("JKL");
    expect(trajectoryA.positions[1].approachSegment.thresholdDistanceMeters).toBe(7.8);
    expect(trajectoryA.positions[1].approachSegment.verticalDevMeters).toBe(9.1);
    expect(trajectoryA.positions[1].approachSegment.horizontalDevMeters).toBe(2.3);
    expect(trajectoryA.positions[1].approachSegment.normalizedEuclideanDistance).toBe(4.5);
  });

  test("should throw if the template is empty", () => {
    expect(() => {
      constructTrajectoryCollection([]);
    }).toThrow();
  });

  test("should throw if a trajectory in the template contains no positions", () => {
    expect(() => {
      constructTrajectoryCollection([
        {
          icao24: icao24A,
          callsign: callsignA,
          category: categoryA,
          positions: {}
        }
      ]);
    }).toThrow();
  });
});
