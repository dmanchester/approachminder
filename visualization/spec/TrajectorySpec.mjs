import Trajectory from "../lib/Trajectory.mjs";
import { Cartesian3, JulianDate } from "cesium";
import AircraftProfile from "../lib/AircraftProfile.mjs";
import TimeBasedPosition from "../lib/TimeBasedPosition.mjs";

describe("Trajectory", function() {

  const tbp0a = new TimeBasedPosition(JulianDate.fromIso8601("2023-01-01T00:00:01Z"), Cartesian3.fromDegrees(1, 2, 3));
  const tbp0b = new TimeBasedPosition(JulianDate.fromIso8601("2023-01-01T00:00:01Z"), Cartesian3.fromDegrees(4, 5, 6));
  const tbp1 =  new TimeBasedPosition(JulianDate.fromIso8601("2023-01-01T00:01:01Z"), Cartesian3.fromDegrees(7, 8, 9));

  it("should provide tidied TimeBasedPositions: time-ordered, and only one per time instant", function() {

    const untidyTimeBasedPositions = [tbp1, tbp0a, tbp0b];  // intentionally passing positions in non-chronological order
    const trajectory = new Trajectory(new AircraftProfile("abc123"), untidyTimeBasedPositions);
    const tidyTimeBasedPositions = trajectory.timeBasedPositions;

    expect(tidyTimeBasedPositions).toEqual([tbp0a, tbp1]);
  });

  it("earliestTime() should give the earliest time", function() {
    const trajectory = new Trajectory(new AircraftProfile("abc123"), [tbp0a, tbp1]);  // TODO Factor out this `trajectory`?
    const expectedDate = JulianDate.fromIso8601("2023-01-01T00:00:01Z");

    expect(trajectory.earliestTime().equals(expectedDate)).toBeTrue();
  });

  it("latestTime() should give the latest time", function() {
    const trajectory = new Trajectory(new AircraftProfile("abc123"), [tbp0a, tbp1]);
    const expectedDate = JulianDate.fromIso8601("2023-01-01T00:01:01Z");

    expect(trajectory.latestTime().equals(expectedDate)).toBeTrue();
  });

  describe("latestPositionWithinWindow()", function() {

    const trajectory = new Trajectory(new AircraftProfile("abc123"), [tbp0a, tbp1]);

    it("should return null when there's no position within the window", function() {
      const tbp = trajectory.latestPositionWithinWindow(JulianDate.fromIso8601("2023-01-01T00:03:01Z"), 60);  // window starts after tbp1
      expect(tbp).toBeUndefined();
    });

    it("should return the latest position when there are multiple within the window", function() {
      const tbp = trajectory.latestPositionWithinWindow(JulianDate.fromIso8601("2023-01-01T00:01:02Z"), 62);
      expect(tbp).toEqual(tbp1);
    });

    it("should return a position from the window's end time", function() {
      const tbp = trajectory.latestPositionWithinWindow(JulianDate.fromIso8601("2023-01-01T00:01:01Z"), 60);
      expect(tbp).toEqual(tbp1);
    });

    it("should return a position from the window's start time", function() {
      const tbp = trajectory.latestPositionWithinWindow(JulianDate.fromIso8601("2023-01-01T00:00:01Z"), 60);
      expect(tbp).toEqual(tbp0a);
    });
  });
});
