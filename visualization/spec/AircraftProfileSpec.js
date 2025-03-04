import AircraftProfile from "../lib/AircraftProfile.js";

describe("AircraftProfile", function() {

  it("should store attributes correctly", function() {
    const icao24 = "abc123";
    const callsign = "my callsign";
    const category = "my category";
    const aircraftProfile = new AircraftProfile(icao24, callsign, category);

    expect(aircraftProfile.icao24).toEqual(icao24);
    expect(aircraftProfile.callsign).toEqual(callsign);
    expect(aircraftProfile.category).toEqual(category);
  });
});
