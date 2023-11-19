import { Cartesian3, JulianDate } from "cesium";

class TimeBasedPosition {

  /**
   * Construct an instance.
   * @param {JulianDate} time
   * @param {Cartesian3} position
   */
  constructor(time, position) {
    this.time = time;
    this.position = position;
  }
}

export default TimeBasedPosition;
