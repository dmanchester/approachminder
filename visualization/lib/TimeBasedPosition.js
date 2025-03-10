import { JulianDate } from "cesium";

class TimeBasedPosition {

  /**
   * Construct an instance.
   * @param {JulianDate} time
   * @param {number} longitude
   * @param {number} latitude
   * @param {number} altitude
   * @param {boolean} onGround
   * @param {?number} velocity
   * @param {?number} trueTrack
   * @param {?number} verticalRate
   * @param {?string} squawk
   * @param {?ApproachSegment} approachSegment
   */
  constructor(time, longitude, latitude, altitude, onGround, velocity, trueTrack, verticalRate, squawk, approachSegment) {
    this.time = time;
    this.longitude = longitude;
    this.latitude = latitude;
    this.altitude = altitude;
    this.onGround = onGround;
    this.velocity = velocity;
    this.trueTrack = trueTrack;
    this.verticalRate = verticalRate;
    this.squawk = squawk;
    this.approachSegment = approachSegment;
  }
}

export default TimeBasedPosition;
