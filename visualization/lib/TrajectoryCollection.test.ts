import TrajectoryCollection from "./TrajectoryCollection";

import { JulianDate } from "cesium";

import { constructTrajectory } from "../test/helpers";

import { describe, expect, test } from "vitest";

const trajectoryATime0 = "2026-01-02T00:01:01Z";  // earliest time of any trajectory
const trajectoryATime1 = "2026-01-02T00:01:06Z";
const trajectoryBTime0 = "2026-01-02T00:03:02Z";
const trajectoryBTime1 = "2026-01-02T00:03:08Z";  // latest time of any trajectory
const trajectoryCTime0 = "2026-01-02T00:01:10Z";
const trajectoryCTime1 = "2026-01-02T00:01:15Z";
const trajectoryDTime0 = "2026-01-02T00:01:30Z";
const trajectoryDTime1 = "2026-01-02T00:02:15Z";

const trajectoryA = constructTrajectory("foo", [ trajectoryATime0, trajectoryATime1 ]);
const trajectoryB = constructTrajectory("bar", [ trajectoryBTime0, trajectoryBTime1 ]);
const trajectoryC = constructTrajectory("baz", [ trajectoryCTime0, trajectoryCTime1 ]);
const trajectoryD = constructTrajectory("baz", [ trajectoryDTime0, trajectoryDTime1 ]);

const trajectoryCollection = new TrajectoryCollection([trajectoryA, trajectoryB, trajectoryC, trajectoryD]);

test("class should retain the ordering of the trajectories as supplied to the constructor", () => {
  const icao24Values = trajectoryCollection.trajectories.map(trajectory => trajectory.icao24);
  expect(icao24Values).toEqual(["foo", "bar", "baz", "baz"]);
});

test("constructor should throw if it receives no Trajectory instances",  () => {
  expect(() => {
    new TrajectoryCollection([]);
  }).toThrow();
});

test("earliestTime() should give the earliest time of any trajectory", () => {
  expect(trajectoryCollection.earliestTime().equals(JulianDate.fromIso8601(trajectoryATime0)));
});

test("latestTime() should give the latest time of any trajectory", () => {
  expect(trajectoryCollection.latestTime().equals(JulianDate.fromIso8601(trajectoryBTime1)));
});

describe("latestPositionsWithinWindow()", () => {

  const latestPositions = trajectoryCollection.latestPositionsWithinWindow(JulianDate.fromIso8601("2026-01-02T00:02:00Z"), 60);

  test("should include the latest position in the window from an aircraft's single trajectory in the collection", () => {
    const filteredPositions = latestPositions.filter(position => position.trajectory.icao24 === "foo");
    expect(filteredPositions.length).toBe(1);
    expect(filteredPositions[0].time.equals(JulianDate.fromIso8601(trajectoryATime1)));
  });

  test("should include the latest position in the window across all of an aircraft's multiple trajectories in the collection", () => {
    const filteredPositions = latestPositions.filter(position => position.trajectory.icao24 === "baz");
    expect(filteredPositions.length).toBe(1);
    expect(filteredPositions[0].time.equals(JulianDate.fromIso8601(trajectoryDTime0)));
  });

  test("should not reflect an aircraft that does not have a trajectory intersecting the window", () => {
    const filteredPositions = latestPositions.filter(position => position.trajectory.icao24 === "bar");
    expect(filteredPositions.length).toBe(0);
  });
});
