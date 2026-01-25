/**
 * An aircraft, as has been observed on a trajectory.
 *
 * Combines fixed attributes of the aircraft (icao24 identifier, category) with operational ones (callsign).
 */
class AircraftProfile {
  constructor(
    public readonly icao24: string,
    public readonly callsign: string | null,
    public readonly category: string | null
  ) {}
}

export default AircraftProfile;
