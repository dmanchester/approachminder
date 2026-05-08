export class ApproachSegment {
  constructor(
    public readonly airport: string,
    public readonly threshold: string,
    public readonly thresholdDistanceMeters: number,
    public readonly verticalDevMeters: number,
    public readonly horizontalDevMeters: number,
    public readonly normalizedEuclideanDistance: number
  ) {}
}
