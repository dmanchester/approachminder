import TimeBasedPosition from "../lib/TimeBasedPosition.js";
import { JulianDate } from "cesium";

describe("TimeBasedPosition", function() {

  it("should store attributes correctly", function() {
    const time = JulianDate.fromIso8601("2023-01-01T00:00:01Z");
    const timeBasedPosition = new TimeBasedPosition(time, 1, 2, 3, true, 5, 6, 7, "A");

    expect(timeBasedPosition.time).toEqual(time);
    expect(timeBasedPosition.longitude).toEqual(1);
    expect(timeBasedPosition.latitude).toEqual(2);
    expect(timeBasedPosition.altitude).toEqual(3);
    expect(timeBasedPosition.onGround).toEqual(true);
    expect(timeBasedPosition.velocity).toEqual(5);
    expect(timeBasedPosition.trueTrack).toEqual(6);
    expect(timeBasedPosition.verticalRate).toEqual(7);
    expect(timeBasedPosition.squawk).toEqual("A");
  });
});
