/**
 * An aircraft, as has been observed on a trajectory.
 *
 * Combines fixed attributes of the aircraft (icao24 identifier, category) with operational ones
 * (callsign). */
class AircraftProfile {
  /**
   * Construct an instance.
   * @param {string} icao24
   * @param {?string} callsign
   * @param {?string} category
   */
  constructor(icao24, callsign, category) {
    this.icao24 = icao24;
    this.callsign = callsign;
    this.category = category;
  }
}

export default AircraftProfile;
