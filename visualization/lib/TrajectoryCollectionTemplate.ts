/**
 * The TrajectoryCollectionTemplate type facilitates the creation of TrajectoryCollection instances.
 *
 * Its structure matches that of file "visualization/src/data.json" upon parsing.
 *
 * The intent is that code handling that data upon parsing/import will apply this type to it for
 * IO.constructTrajectoryCollection() to build a TrajectoryCollection instance.
 */
export type TrajectoryCollectionTemplate = Array<{
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