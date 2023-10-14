import AircraftProfile from "../lib/AircraftProfile.mjs";

describe("AircraftProfile", function() {

  it("should store an icao24 identifier", function() {
    const icao24 = "abc123";
    const aircraftProfile = new AircraftProfile(icao24);

    expect(aircraftProfile.icao24).toEqual(icao24);
  });
});
