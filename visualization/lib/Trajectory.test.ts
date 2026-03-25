import AircraftProfile from "./AircraftProfile";
import Trajectory from "./Trajectory";

import { JulianDate } from "cesium";

import { constructTrajectory } from "../test/helpers";

import { describe, expect, test } from "vitest";

// basicTrajectory, used by many of the tests in this file, has three positions in a 30-second time window from
// 00:01:01Z - 00:01:31Z on 2026-01-01.
//
// The positions are intentionally passed in non-chronological order (time1, time2, time0). This facilitates the test,
// "constructor should time-order its Positions".

const time0 = "2026-01-01T00:01:01Z";
const time1 = "2026-01-01T00:01:06Z";
const time2 = "2026-01-01T00:01:31Z";

const time0AsJulianDate = JulianDate.fromIso8601(time0);
const time1AsJulianDate = JulianDate.fromIso8601(time1);
const time2AsJulianDate = JulianDate.fromIso8601(time2);

const basicTrajectory = constructTrajectory("foo", [ time1, time2, time0 ]);

test("constructor should populate its Positions with a reference to itself",  () => {
  expect(basicTrajectory.positions[0].trajectory).toBe(basicTrajectory);
});

test("constructor should time-order its Positions",  () => {
  expect(basicTrajectory.positions[0].time.equals(time0AsJulianDate)).toBeTruthy();
  expect(basicTrajectory.positions[1].time.equals(time1AsJulianDate)).toBeTruthy();
  expect(basicTrajectory.positions[2].time.equals(time2AsJulianDate)).toBeTruthy();
});

test("constructor should throw if it receives no PositionTemplate instances",  () => {
  expect(() => {
    new Trajectory(new AircraftProfile("foo", null, null), Object.fromEntries([]));
  }).toThrow();
});

test("earliestTime() should give the earliest time", () => {
  expect(basicTrajectory.earliestTime().equals(time0AsJulianDate)).toBeTruthy();
});

test("latestTime() should give the latest time", () => {
  expect(basicTrajectory.latestTime().equals(time2AsJulianDate)).toBeTruthy();
});

describe("latestPositionWithinWindow()", () => {

  test("should return undefined when there's no Position within the window",  () => {
    const position = basicTrajectory.latestPositionWithinWindow(JulianDate.fromIso8601("2026-01-01T00:03:01Z"), 60);  // window starts after time2
    expect(position).toBeUndefined();
  });

  test("should return the latest Position when there are multiple within the window",  () => {
    const position = basicTrajectory.latestPositionWithinWindow(JulianDate.fromIso8601("2026-01-01T00:01:11Z"), 20);  // window includes time0 and time1
    expect(position.time.equals(time1AsJulianDate)).toBeTruthy();
  });

  test("should return a Position from the window's end time",  () => {
    const position = basicTrajectory.latestPositionWithinWindow(JulianDate.fromIso8601(time0), 10);  // window ends at time0
    expect(position.time.equals(time0AsJulianDate)).toBeTruthy();
  });

  test("should return a Position from the window's start time",  () => {
    const position = basicTrajectory.latestPositionWithinWindow(JulianDate.fromIso8601("2026-01-01T00:01:51Z"), 20);  // window starts at time2
    expect(position.time.equals(time2AsJulianDate)).toBeTruthy();
  });
});
