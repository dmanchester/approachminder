package com.dmanchester.approachminder

// FIXME Throughout this file, do I have a lot more public fields than I intend?

class Airport private(val icaoID: String, val referencePoint: HasLongLat, runwaySurfaceTemplates: Seq[RunwaySurfaceTemplate]) {

  val geographicCalculator = GeographicCalculator(referencePoint)

  // TODO Enforce uniqueness of runway names

  /**
   * The ordering of `runwaySurfaces` matches that of the `RunwaySurfaceTemplate`s provided at
   * construction.
   */
  val runwaySurfaces = runwaySurfaceTemplates.map(RunwaySurface(_))  // construct runway surfaces and thresholds via the RunwaySurface pseudo-constructor

  /**
   * Each group of two contiguous thresholds maps to a runway surface, with the surfaces having the
   * same order as the `RunwaySurfaceTemplate`s provided at construction.
   *
   * Within a group of two thresholds, the first one maps to the first one specified in
   * `RunwaySurfaceTemplate`.
   */
  val thresholds = runwaySurfaces.flatMap(_.thresholdsAsSeq)

  // TODO Figure out if I'm doing this inner class stuff "right"; I guess this is path-dependent types; it's true that we don't want to let someone instantiate, say, a runway surface without an airport. Is that the right construct, or are we creating tons of unnecessary class objects? See also https://moi.vonos.net/java/scala/#nested-inner-classes-and-path-dependent-types
  // TODO Also see:
  // https://docs.scala-lang.org/tour/inner-classes.html
  // https://www.john-cd.com/cheatsheets/Scala/Scala_Language/#path-dependent-classes
  // https://alvinalexander.com/scala/how-to-create-inner-classes-in-scala-differences-java/
  // https://stackoverflow.com/questions/2183954/referring-to-the-type-of-an-inner-class-in-scala
  // https://users.scala-lang.org/t/question-regarding-pound-sign/1628

  def thresholdByName(name: String) = {
    thresholds.find(_.name == name)
  }

  class RunwaySurface private(val widthInMeters: Double, threshold0Name: String, threshold0Left: HasLongLat, threshold0Center: HasLongLat, threshold0Right: HasLongLat, threshold1Name: String, threshold1Left: HasLongLat, threshold1Center: HasLongLat, threshold1Right: HasLongLat) {

    // TODO Offer naming of runway surfaces (not just thresholds)?

    val threshold0 = RunwayThreshold(threshold0Name, threshold0Left, threshold0Center, threshold0Right)
    val threshold1 = RunwayThreshold(threshold1Name, threshold1Left, threshold1Center, threshold1Right)
    val thresholdsAsSeq = Seq(threshold0, threshold1)
    val rectangle = Polygon(Seq(threshold0Left, threshold0Right, threshold1Left, threshold1Right))

    def airport = Airport.this

    def geographicCalculator = airport.geographicCalculator  // FIXME Can I delete?

    def contains(point: HasLongLat): Boolean = geographicCalculator.contains(rectangle, point)

    private def oppositeThreshold(threshold: RunwaySurface#RunwayThreshold): RunwayThreshold = {  // TODO Confirm prefixing with "RunwaySurface#" is "right" way to solve spec's compilation issue
      threshold match {
        case `threshold0` => threshold1
        case `threshold1` => threshold0
        case _ => throw new IllegalArgumentException("Unrecognized threshold!")
      }
    }

    override def toString = s"${this.getClass.getSimpleName}($threshold0Left,$threshold0Right,$threshold1Left,$threshold1Right)"

    class RunwayThreshold private(val name: String, val left: HasLongLat, val center: HasLongLat, val right: HasLongLat) {

      val thresholdSegment = (left, right)

      def runwaySurface = RunwaySurface.this

      def airport = Airport.this

      def geographicCalculator = runwaySurface.geographicCalculator  // FIXME Can I delete?

      /**
       * Determine whether:
       *
       *   * a flight segment crosses this threshold in the inbound direction; and
       *   * the segment's second point lies within the rectangle of the runway surface.
       *
       * If those criteria are met, interpolate the crossing point.
       *
       * @param flightSegment
       * @return
       */
      def interpolateInboundCrossingPoint(flightSegment: (HasLongLat, HasLongLat)): Option[(HasLongLat, Double)] = {
        // TODO After making possible low-level performance improvements, investigate which of these operations is quicker to reject a non-match; order first.
        if (runwaySurface.contains(flightSegment._2)) {
          geographicCalculator.intersection(flightSegment, thresholdSegment)
        } else {
          None
        }
      }

      // TODO Need a test for "angle"

      /**
       * Calculate the angle from the center of the threshold to a point.
       *
       * @param point
       * @return
       */
      def angle(point: HasLongLat): PolarAngle = {
        geographicCalculator.angle(center, point)
      }

      /**
       * Calculate the distance in meters from the center of the threshold to a point.
       *
       * TODO It seems odd I didn't have cause to add this method until June 2024...although only previous (non-test)
       * use of geographicCalculator.distanceInMeters was in ExtractionAndEstimation.interpolate, which "speaks" at a
       * lower level than thresholds etc.?
       *
       * TODO Add test coverage? (Do I have any for "angle" method?)
       *
       * @param point
       * @return
       */
      def distanceInMeters(point: HasLongLat): Double = {
        geographicCalculator.distanceInMeters(center, point)
      }

      /**
       * Calculate a point on the runway centerline.
       *
       * @param relativePosition The position of the point relative to this threshold. 0.0 = on this threshold; 1.0 = on
       *                         the opposite threshold. A value would typically be between 0.0 and 1.0, but it need
       *                         not be.
       * @return
       */
      def pointOnRunwayCenterline(relativePosition: Double): HasLongLat = {  // TODO Here and (many) other places, return class instead of trait?
        geographicCalculator.pointOnSegment((center, oppositeThreshold.center), relativePosition)
      }

      def oppositeThreshold = runwaySurface.oppositeThreshold(this)

      override def toString = s"${this.getClass.getSimpleName}($name,$left,$center,$right)"
    }

    object RunwayThreshold {
      def apply(name: String, left: HasLongLat, center: HasLongLat, right: HasLongLat): RunwayThreshold = new RunwayThreshold(name, left, center, right)
    }
  }

  object RunwaySurface {

    def apply(template: RunwaySurfaceTemplate): RunwaySurface = {

      val (threshold0Left, threshold0Right) = thresholdLeftAndRight(template.widthInMeters, template.threshold0Center, template.threshold1Center)
      val (threshold1Left, threshold1Right) = thresholdLeftAndRight(template.widthInMeters, template.threshold1Center, template.threshold0Center)
      new RunwaySurface(template.widthInMeters, template.threshold0Name, threshold0Left, template.threshold0Center, threshold0Right, template.threshold1Name, threshold1Left, template.threshold1Center, threshold1Right)
    }

    /**
     * Given the width of a runway, the center point of a runway threshold, and
     * the center point of the opposite threshold, determine the left and right
     * points of the threshold.
     *
     * TODO Is this the "best" object for this method?
     *
     * @param widthInMeters
     * @param center
     * @param oppositeCenter
     * @return left and right points of the threshold ("left" and "right" as seen
     *         from a landing aircraft)
     */
    private def thresholdLeftAndRight(widthInMeters: Double, center: HasLongLat, oppositeCenter: HasLongLat): (HasLongLat, HasLongLat) = {

      val halfWidthInMeters = widthInMeters / 2

      val left = geographicCalculator.rotateAboutArbitraryOriginAndScaleToDistance(oppositeCenter, center, 90.0, halfWidthInMeters)
      val right = geographicCalculator.rotateAboutArbitraryOriginAndScaleToDistance(oppositeCenter, center, -90.0, halfWidthInMeters)

      (left, right)
    }
  }
}

object Airport {
  def apply(icaoID: String, referencePoint: HasLongLat, runwaySurfaceTemplates: Seq[RunwaySurfaceTemplate]): Airport = new Airport(icaoID, referencePoint, runwaySurfaceTemplates)
}
