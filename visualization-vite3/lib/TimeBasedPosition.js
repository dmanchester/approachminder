import { JulianDate } from "cesium";

class TimeBasedPosition {

  /**
   * Construct an instance.
   * @param {JulianDate} time
   * @param {Number} longitude
   * @param {Number} latitude
   * @param {Number} altitude
   */
  constructor(time, longitude, latitude, altitude) {
    this.time = time;
    this.longitude = longitude;
    this.latitude = latitude;
    this.altitude = altitude;
  }
}

export default TimeBasedPosition;
