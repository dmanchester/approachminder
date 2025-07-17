package com.dmanchester.approachminder.typeswithbehavior

import com.dmanchester.approachminder.typeswithoutbehavior.{HasLongLat, RunwaySurfaceTemplate}
import com.dmanchester.approachminder.{GeographicCalculator, Polygon}

/**
 * An airport, along with--as inner classes--its runway surfaces and the runways themselves. (Each runway surface is
 * considered two runways, depending on the direction from which it's approached.)
 *
 * Each runway is identified by a name that is unique (at that airport).
 *
 * Airport, RunwaySurface, and Runway are *not* case classes because the constructor parameters for Airport and
 * RunwaySurface differ significantly from the desired class fields.
 */
class Airport private(val icaoID: String, val referencePoint: HasLongLat, runwaySurfaceTemplates: Iterable[RunwaySurfaceTemplate]) {

  val geographicCalculator: GeographicCalculator = GeographicCalculator(referencePoint)

  /**
   * The ordering of `runwaySurfaces` and `runways` matches that of the `RunwaySurfaceTemplate`s provided at
   * construction.
   */
  val runwaySurfaces: Seq[RunwaySurface] = runwaySurfaceTemplates.map(RunwaySurface(_)).toSeq
  val runways: Seq[RunwaySurface#Runway] = runwaySurfaces.flatMap { runwaySurface => Seq(runwaySurface.runway0, runwaySurface.runway1) }

  private val namesToRunways: Map[String, RunwaySurface#Runway] = runways.foldLeft(Map.empty[String, RunwaySurface#Runway]) { case (theMap, runway) =>

      if (theMap.contains(runway.name)) {
        throw new IllegalArgumentException(s"Duplicate runway name found: '${runway.name}'!")
      }

      theMap.updated(runway.name, runway)
  }

  /**
   * Get a runway by name.
   *
   * @param name The name.
   * @throws java.util.NoSuchElementException If a runway by that name does not exist.
   * @return The runway.
   */
  @throws(classOf[NoSuchElementException])
  def getRunwayByName(name: String): RunwaySurface#Runway = namesToRunways(name)

  override def toString: String = s"${this.getClass.getSimpleName}($icaoID,$referencePoint,$runwaySurfaces)"

  class RunwaySurface private(val widthInMeters: Double, runway0Name: String, runway0ThresholdLeft: HasLongLat, runway0ThresholdCenter: HasLongLat, runway0ThresholdRight: HasLongLat, runway1Name: String, runway1ThresholdLeft: HasLongLat, runway1ThresholdCenter: HasLongLat, runway1ThresholdRight: HasLongLat) {

    // Obtain references to the outer class and any needed members first.
    val airport: Airport = Airport.this
    val geographicCalculator: GeographicCalculator = airport.geographicCalculator

    val runway0: Runway = Runway(runway0Name, runway0ThresholdLeft, runway0ThresholdCenter, runway0ThresholdRight)
    val runway1: Runway = Runway(runway1Name, runway1ThresholdLeft, runway1ThresholdCenter, runway1ThresholdRight)

    private val rectangle = Polygon(Seq(runway0ThresholdLeft, runway0ThresholdRight, runway1ThresholdLeft, runway1ThresholdRight))

    /**
     * Test whether the runway surface contains a point.
     *
     * @param point The point.
     * @return The test result.
     */
    def contains(point: HasLongLat): Boolean = geographicCalculator.contains(rectangle, point)

    private def oppositeRunway(runway: this.Runway): Runway = {
      runway match {
        case `runway0` => runway1
        case `runway1` => runway0
        case _ => throw new IllegalArgumentException("Unrecognized runway!")
      }
    }

    override def toString: String = s"${this.getClass.getSimpleName}($widthInMeters,$runway0,$runway1)"

    class Runway private(val name: String, val thresholdLeft: HasLongLat, val thresholdCenter: HasLongLat, val thresholdRight: HasLongLat) {

      // Obtain references to the outer classes and any needed members first.
      val airport: Airport = Airport.this
      val surface: RunwaySurface = RunwaySurface.this
      val geographicCalculator: GeographicCalculator = surface.geographicCalculator

      lazy val opposite: Runway = oppositeRunway(this)  // Why "lazy val"? When first runway is instantiated, its opposite doesn't yet exist.

      private val thresholdSegment = (thresholdLeft, thresholdRight)

      /**
       * Test whether:
       *
       *   * a flight segment crosses the runway's threshold in the inbound direction; and
       *   * the segment's second point lies within the rectangle of the runway surface.
       *
       * @param flightSegment The flight segment.
       * @return If the flight segment meets the above criteria: the interpolated crossing point, as well as where the
       *         crossing point lies along the segment, expressed as a percentage from the segment's first point;
       *         wrapped in Some. -- Or, None, if the flight segment does not meet the above criteria.
       */
      def testForInboundThresholdCrossing(flightSegment: (HasLongLat, HasLongLat)): Option[(HasLongLat, Double)] = {
        Option.when(surface.contains(flightSegment._2)) {
          geographicCalculator.intersection(flightSegment, thresholdSegment)
        }.flatten
      }

      /**
       * Calculate the distance from an arbitrary point to the center point of the runway's threshold.
       *
       * TODO Add tests.
       *
       * @param point The arbitrary point.
       * @return The distance, in meters.
       */
      def distanceInMetersToThresholdCenter(point: HasLongLat): Double = {
        geographicCalculator.distanceInMeters(thresholdCenter, point)
      }

      /**
       * Calculate a point on the runway's centerline.
       *
       * @param relativePosition The position of the point relative to the runway's threshold. 0.0 = on the threshold;
       *                         1.0 = on the opposite runway's threshold. A value would typically be between 0.0 and
       *                         1.0, but it need not be.
       * @return The point.
       */
      def pointOnRunwayCenterline(relativePosition: Double): HasLongLat = {
        geographicCalculator.pointOnSegment((thresholdCenter, opposite.thresholdCenter), relativePosition)
      }

      override def toString: String = s"${this.getClass.getSimpleName}($name,$thresholdLeft,$thresholdCenter,$thresholdRight)"
    }

    object Runway {
      def apply(name: String, thresholdLeft: HasLongLat, thresholdCenter: HasLongLat, thresholdRight: HasLongLat): Runway = new Runway(name, thresholdLeft, thresholdCenter, thresholdRight)
    }
  }

  object RunwaySurface {

    def apply(template: RunwaySurfaceTemplate): RunwaySurface = {
      val (runway0ThresholdLeft, runway0ThresholdRight) = thresholdLeftAndRight(template.widthInMeters, template.runway0ThresholdCenter, template.runway1ThresholdCenter)
      val (runway1ThresholdLeft, runway1ThresholdRight) = thresholdLeftAndRight(template.widthInMeters, template.runway1ThresholdCenter, template.runway0ThresholdCenter)
      new RunwaySurface(template.widthInMeters, template.runway0Name, runway0ThresholdLeft, template.runway0ThresholdCenter, runway0ThresholdRight, template.runway1Name, runway1ThresholdLeft, template.runway1ThresholdCenter, runway1ThresholdRight)
    }

    /**
     * Determine the left and right points of a runway threshold.
     *
     * @param widthInMeters The width of the runway, in meters.
     * @param thresholdCenter The center point of the threshold.
     * @param oppositeThresholdCenter The center point of the opposite runway's threshold.
     * @return The left and right points of the threshold ("left" and "right" as seen from a landing aircraft).
     */
    private def thresholdLeftAndRight(widthInMeters: Double, thresholdCenter: HasLongLat, oppositeThresholdCenter: HasLongLat): (HasLongLat, HasLongLat) = {

      val halfWidthInMeters = widthInMeters / 2

      val left = geographicCalculator.rotateAboutArbitraryOriginAndScaleToDistance(oppositeThresholdCenter, thresholdCenter, 90.0, halfWidthInMeters)
      val right = geographicCalculator.rotateAboutArbitraryOriginAndScaleToDistance(oppositeThresholdCenter, thresholdCenter, -90.0, halfWidthInMeters)

      (left, right)
    }
  }
}

object Airport {

  /**
   * Create an airport, its runway surfaces, and the runways themselves.
   *
   * @param icaoID The airport's ICAO ID (e.g., "KSFO").
   * @param referencePoint A reference point at the airport. Used to initialize the GeographicCalculator used for
   *                       airport-related calculations. -- Only needs to be approximate: actual distance calculations
   *                       rely on runway geometry.
   * @param runwaySurfaceTemplates Templates for construction of the runway surfaces and runways. Runway names within
   *                               the templates must be unique (at a given airport).
   * @throws java.lang.IllegalArgumentException If a runway name is duplicated within the runway surface templates.
   * @return The airport, with the runway surfaces and runways as inner classes.
   */
  @throws(classOf[IllegalArgumentException])
  def apply(icaoID: String, referencePoint: HasLongLat, runwaySurfaceTemplates: Iterable[RunwaySurfaceTemplate]): Airport = new Airport(icaoID, referencePoint, runwaySurfaceTemplates)
}
