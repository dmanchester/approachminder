import AircraftProfile from "./AircraftProfile";
import TimeBasedPosition from "./TimeBasedPosition";
import Trajectory from "./Trajectory";

import { describe, expect, test } from "vitest";
import { JulianDate } from "cesium";

const minimalAircraftProfile = new AircraftProfile("foo", null, null);

function minimalTimeBasedPosition(time: JulianDate): TimeBasedPosition {
  return new TimeBasedPosition(time, 0, 0, 0, false, null, null, null, null, null)
}

const tbp0a = minimalTimeBasedPosition(JulianDate.fromIso8601("2023-01-01T00:00:01Z"));
const tbp0b = minimalTimeBasedPosition(JulianDate.fromIso8601("2023-01-01T00:00:01Z"));
const tbp1 =  minimalTimeBasedPosition(JulianDate.fromIso8601("2023-01-01T00:01:01Z"));

test("should provide tidied TimeBasedPositions: time-ordered, and only one per time instant",  () => {

  const untidyTimeBasedPositions = [tbp1, tbp0a, tbp0b];  // intentionally passing positions in non-chronological order
  const trajectory = new Trajectory(minimalAircraftProfile, untidyTimeBasedPositions);
  const tidyTimeBasedPositions = trajectory.timeBasedPositions;

  expect(tidyTimeBasedPositions).toEqual([tbp0a, tbp1]);
});

test("earliestTime() should give the earliest time", () => {
  const trajectory = new Trajectory(minimalAircraftProfile, [tbp0a, tbp1]);
  const expectedDate = JulianDate.fromIso8601("2023-01-01T00:00:01Z");

  expect(trajectory.earliestTime().equals(expectedDate)).toEqual(true);
});

test("latestTime() should give the latest time", () => {
  const trajectory = new Trajectory(minimalAircraftProfile, [tbp0a, tbp1]);
  const expectedDate = JulianDate.fromIso8601("2023-01-01T00:01:01Z");

  expect(trajectory.latestTime().equals(expectedDate)).toEqual(true);
});

describe("latestPositionWithinWindow()", () => {

  const trajectory = new Trajectory(minimalAircraftProfile, [tbp0a, tbp1]);

  test("should return null when there's no position within the window",  () => {
    const tbp = trajectory.latestPositionWithinWindow(JulianDate.fromIso8601("2023-01-01T00:03:01Z"), 60);  // window starts after tbp1
    expect(tbp).toBeUndefined();
  });

  test("should return the latest position when there are multiple within the window",  () => {
    const tbp = trajectory.latestPositionWithinWindow(JulianDate.fromIso8601("2023-01-01T00:01:02Z"), 62);
    expect(tbp).toEqual(tbp1);
  });

  test("should return a position from the window's end time",  () => {
    const tbp = trajectory.latestPositionWithinWindow(JulianDate.fromIso8601("2023-01-01T00:01:01Z"), 60);
    expect(tbp).toEqual(tbp1);
  });

  test("should return a position from the window's start time",  () => {
    const tbp = trajectory.latestPositionWithinWindow(JulianDate.fromIso8601("2023-01-01T00:00:01Z"), 60);
    expect(tbp).toEqual(tbp0a);
  });
});
