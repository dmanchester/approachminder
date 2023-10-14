/**
 * An aircraft, as has been observed on a trajectory. TODO Once tail number/flight number added, note that it combines
 * physical and operational attributes. */
class AircraftProfile {
  /**
   * Construct an instance.
   * @param {string} icao24
   */
  constructor(icao24) {
    this.icao24 = icao24;
  }
}

export default AircraftProfile;
