export type ParsedJSON = Array<{
  icao24: string,
  callsign: string | null,
  category: string | null,
  positions: Record<string /* ISO-8601 date */, {
    longitude: number,
    latitude: number,
    altitude: number,
    onGround: boolean,
    velocity: number | null,
    trueTrack: number | null,
    verticalRate: number | null,
    squawk : string | null,
    approachSegment: {
      airport: string,
      threshold: string,
      thresholdDistanceMeters: number,
      verticalDevMeters: number,
      horizontalDevMeters: number,
      normalizedEuclideanDistance: number
    } | null
  }>
}>