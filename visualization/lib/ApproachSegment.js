class ApproachSegment {

    /**
     * Construct an instance.
     * @param {string} airport
     * @param {string} threshold
     * @param {number} thresholdDistanceMeters
     * @param {number} verticalDevMeters
     * @param {number} horizontalDevMeters
     * @param {number} normalizedEuclideanDistance
     */

    constructor(airport, threshold, thresholdDistanceMeters, verticalDevMeters, horizontalDevMeters, normalizedEuclideanDistance) {
        this.airport = airport;
        this.threshold = threshold;
        this.thresholdDistanceMeters = thresholdDistanceMeters;
        this.verticalDevMeters = verticalDevMeters;
        this.horizontalDevMeters = horizontalDevMeters;
        this.normalizedEuclideanDistance = normalizedEuclideanDistance;
    }
}

export default ApproachSegment;
