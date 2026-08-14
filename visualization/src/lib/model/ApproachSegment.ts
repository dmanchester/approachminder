/**
 * Given a position that has been deemed part of an approach to a runway, an ApproachSegment provides information about
 * the approach at that position: How far the aircraft is from the runway threshold, how much the aircraft is deviating
 * from the approach, etc.
 */
export class ApproachSegment {
  constructor(
    public readonly airport: string,
    public readonly threshold: string,
    public readonly thresholdDistanceMeters: number,
    public readonly verticalDevMeters: number,
    public readonly horizontalDevMeters: number,
    public readonly normalizedEuclideanDistance: number,
  ) {}
}
