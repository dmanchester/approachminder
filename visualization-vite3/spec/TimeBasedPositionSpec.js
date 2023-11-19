import TimeBasedPosition from "../lib/TimeBasedPosition.js";
import { Cartesian3, JulianDate } from "cesium";

describe("TimeBasedPosition", function() {

  it("should store a time and a position", function() {
    const time = JulianDate.fromIso8601("2023-01-01T00:00:01Z");
    const position = Cartesian3.fromDegrees(1, 2, 3);
    const timeBasedPosition = new TimeBasedPosition(time, position);

    expect(timeBasedPosition.time).toEqual(time);
    expect(timeBasedPosition.position).toEqual(position);
  });
});
