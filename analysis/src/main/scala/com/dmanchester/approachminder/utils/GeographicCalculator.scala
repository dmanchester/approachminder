package com.dmanchester.approachminder.utils

import com.dmanchester.approachminder.typeswithbehavior.PolarAngle
import com.dmanchester.approachminder.typeswithoutbehavior.{AngleAndRelativePosition, HasLongLat, LongLat, Polygon}
import org.geotools.geometry.jts.{JTS, JTSFactoryFinder}
import org.geotools.measure.Units
import org.geotools.referencing.CRS
import org.geotools.referencing.crs.DefaultGeographicCRS
import org.locationtech.jts.algorithm.Angle
import org.locationtech.jts.geom.{Coordinate, LineSegment, Point}
import org.locationtech.jts.math.Vector2D
import org.opengis.referencing.operation.MathTransform

import scala.math.{abs, pow, sqrt}

/**
 * This class facilitates geographic calculations related to distance, angle, and spatial relationships.
 *
 * Where public methods of this class deal in coordinates, they do so via longitude/latitude values.
 *
 * Internally, an instance of this class converts longitude/latitude coordinates to the Universal Transverse
 * Mercator (UTM) projection. The target UTM zone for such conversions is determined from the reference point given at
 * class initialization.
 *
 * Not a case class because the parameters of the apply() pseudo-constructor differ significantly from the desired class
 * fields.
 */
class GeographicCalculator private(private val toUTMTransform: MathTransform, private val toLongLatTransform: MathTransform) {

  private def geometryFactory = JTSFactoryFinder.getGeometryFactory()

  /**
   * Convert a point from longitude and latitude to UTM, providing the result as a JTS Point.
   *
   * @param point The point, expressed as longitude and latitude.
   * @return The point, expressed as UTM via a JTS Point.
   */
  private def toUTMPoint(point: HasLongLat): Point = {
    val longLatPointAsJTS = geometryFactory.createPoint(new Coordinate(point.longitude, point.latitude))
    JTS.transform(longLatPointAsJTS, toUTMTransform).asInstanceOf[Point]
  }

  /**
   * Convert a point from longitude and latitude to UTM, providing the result as a JTS Coordinate.
   *
   * @param point The point, expressed as longitude and latitude.
   * @return The point, expressed as UTM via a JTS Coordinate.
   */
  private def toUTMCoordinate(point: HasLongLat): Coordinate = toUTMPoint(point).getCoordinate

  /**
   * Convert a point from UTM to longitude and latitude.
   *
   * @param utmPoint The point, expressed as UTM.
   * @return The point, expressed as longitude and latitude.
   */
  private def toLongLat(utmPoint: Coordinate): HasLongLat = {
    val utmPointAsJTS = geometryFactory.createPoint(utmPoint)
    val longLatAsCoordinate = JTS.transform(utmPointAsJTS, toLongLatTransform).asInstanceOf[Point].getCoordinate

    LongLat(longLatAsCoordinate.x, longLatAsCoordinate.y)
  }

  /**
   * Calculate the distance between two points.
   *
   * @param pointA The first point.
   * @param pointB The second point.
   * @return The distance, in meters.
   */
  def distanceInMeters(pointA: HasLongLat, pointB: HasLongLat): Double = {

    val pointA_UTM = toUTMCoordinate(pointA)
    val pointB_UTM = toUTMCoordinate(pointB)

    pointA_UTM.distance(pointB_UTM)
  }

  /**
   * Calculate the point that lies at a given angle and distance from an origin.
   *
   * @param origin The origin.
   * @param angle The angle.
   * @param distanceInMeters The distance, in meters.
   * @return The point.
   */
  def pointAtAngleAndDistance(origin: HasLongLat, angle: PolarAngle, distanceInMeters: Double): HasLongLat = {

    val originUTM = toUTMCoordinate(origin)
    val originVectorUTM = Vector2D.create(originUTM)

    val thePointVectorUTM = Vector2D.create(distanceInMeters, 0).rotate(angle.asRadians).add(originVectorUTM)

    toLongLat(thePointVectorUTM.toCoordinate)
  }

  /**
   * Convert a line segment from longitude and latitude to UTM.
   *
   * @param lineSegment The line segment, expressed via longitude and latitude.
   * @return The lines segment, expressed as UTM via a JTS LineSegment.
   */
  private def toUTMLineSegment(lineSegment: (HasLongLat, HasLongLat)): LineSegment = {

    val pointA_UTM = toUTMCoordinate(lineSegment._1)
    val pointB_UTM = toUTMCoordinate(lineSegment._2)

    new LineSegment(pointA_UTM, pointB_UTM)
  }

  /**
   * Determine whether two line segments intersect. If they do, return the point of intersection, as well as where the
   * intersection lies along the first segment.
   *
   * @param lineSegmentA The first line segment.
   * @param lineSegmentB The second line segment.
   * @return If the line segments intersect: the point of intersection, as well as where the intersection lies along the
   *         first segment, expressed as a percentage of the segment's total length as measured from its first point;
   *         wrapped in a Some. Or, None, if they don't intersect.
   */
  def intersection(lineSegmentA: (HasLongLat, HasLongLat), lineSegmentB: (HasLongLat, HasLongLat)): Option[(HasLongLat, Double)] = {

    val lineSegmentA_UTM = toUTMLineSegment(lineSegmentA)
    val lineSegmentB_UTM = toUTMLineSegment(lineSegmentB)

    val pointOfIntersectionUTM = lineSegmentA_UTM.intersection(lineSegmentB_UTM)

    Option(pointOfIntersectionUTM).map { thePointOfIntersectionUTM =>
      val pointOfIntersectionLongLat = toLongLat(thePointOfIntersectionUTM)
      val pctFromSegmentAStartToSegmentAEnd = lineSegmentA_UTM.p0.distance(thePointOfIntersectionUTM) / lineSegmentA_UTM.p0.distance(lineSegmentA_UTM.p1)
      (pointOfIntersectionLongLat, pctFromSegmentAStartToSegmentAEnd)
    }
  }

  /**
   * Determine the point that lies at a relative position along a line segment.
   *
   * @param lineSegment The line segment.
   * @param relativePosition The relative position. 0.0 = at the segment's first point; 1.0 = at the segment's second
   *                         point. A value would typically be between 0.0 and 1.0, but it need not be.
   * @return The point.
   */
  def pointAlongSegment(lineSegment: (HasLongLat, HasLongLat), relativePosition: Double): HasLongLat = {
    val lineSegmentUTM = toUTMLineSegment(lineSegment)
    val point = lineSegmentUTM.pointAlong(relativePosition)
    toLongLat(point)
  }

  /**
   * Determine whether a polygon contains a point.
   *
   * @param polygon The polygon.
   * @param point The point.
   * @return Whether the polygon contains the point.
   */
  def contains(polygon: Polygon, point: HasLongLat): Boolean = {

    // TODO For better performance, check whether the point lies within the polygon's bounding box before proceeding
    //  with UTM conversions etc.? -- If pursuing, profile performance, to ensure there actually is an improvement.

    val perimeterUTMUnclosed = polygon.perimeter.map(toUTMCoordinate)
    val perimeterUTMClosed = perimeterUTMUnclosed :+ perimeterUTMUnclosed.head // Close the polygon by copying the first vertex into the final position.

    val polygonUTM = geometryFactory.createPolygon(perimeterUTMClosed.toArray)

    polygonUTM.contains(toUTMPoint(point))
  }

  /**
   * Rotate a point about an origin and scale its position to a pre-determined distance from that origin.
   *
   * @param point The point.
   * @param origin The origin.
   * @param rotationInDegrees The amount of rotation, in degrees. A positive value indicates counterclockwise rotation.
   * @param distanceInMeters The scaling distance, in meters.
   * @return The transformed point.
   */
  def rotateAboutAnOriginAndScaleToDistance(point: HasLongLat, origin: HasLongLat, rotationInDegrees: Double, distanceInMeters: Double): HasLongLat = {

    val originUTM = toUTMCoordinate(origin)
    val pointUTM = toUTMCoordinate(point)

    val degreesToRadiansConverter = Units.DEGREE_ANGLE.getConverterTo(Units.RADIAN)
    val rotationInRadians = degreesToRadiansConverter.convert(rotationInDegrees)

    val distanceInMetersBeforeScaling = originUTM.distance(pointUTM)
    val scaleFactor = distanceInMeters / distanceInMetersBeforeScaling

    val originVectorUTM = new Vector2D(originUTM) // TODO Flip to static "create"? What about other uses of JTS "new"?

    val transformedPointVectorUTM = new Vector2D(pointUTM).subtract(originVectorUTM).rotate(rotationInRadians).multiply(scaleFactor).add(originVectorUTM)

    toLongLat(transformedPointVectorUTM.toCoordinate)
  }

  /**
   * Rotate a point about a reference point. Provide the result relative to that reference point.
   *
   * Example
   * -------
   * Take (5, 2) as the point, (2, 1) as the reference point, and a rotation of pi/2 radians (90 deg.).
   *
   * The rotation places the point at (1, 4). Relativizing it about (2, 1) leads to (-1, 3), which the method returns.
   *
   * @param point The point.
   * @param referencePoint The reference point.
   * @param rotationInRadians The amount of rotation, in radians. A positive value indicates counterclockwise rotation.
   * @return The rotated and relativized point.
   */
  private def rotateAboutReferencePointAndRelativize(point: Coordinate, referencePoint: Coordinate, rotationInRadians: Double): Coordinate = {

    val pointAsVector = Vector2D.create(point)
    val referencePointAsVector = Vector2D.create(referencePoint)

    pointAsVector.subtract(referencePointAsVector).rotate(rotationInRadians).toCoordinate
  }

  /**
   * Given a directed line segment specified via longitude and latitude, convert it to UTM and rotate it about a
   * reference point such that it points rightward (i.e., is aligned with the positive x-axis). Provide the result
   * relative to that reference point.
   *
   * Example
   * -------
   * (For simplicity, this example relies on Cartesian coordinates, as opposed to ones requiring conversion.)
   *
   * Take (0, 1) as point A, (2, 3) as point B, and (2, 1) as the reference point. (This is a 45-45-90 triangle with leg
   * length 2 and hypotenuse length 2.828.)
   *
   * Directed line segment AB forms an angle of 45 deg. (pi/4 radians; approx. 0.785) with the positive X-axis.
   *
   * Aligning the segment with the positive X-axis entails rotating it -0.785 rad.
   *
   * Upon doing so, point A' lies at (0.586, 2.414), and point B' lies at (3.414, 2.414). (So, they remain a distance of
   * 2.828 apart.)
   *
   * Relativizing those points about (2, 1) leads to (-1.414, 1.414) and (1.414, 1.414).
   *
   * This method returns those two points and the -0.785 rad rotation applied.
   *
   * @param pointA The segment's starting point.
   * @param pointB The segment's end point.
   * @param referencePoint The reference point.
   * @return The starting and end points of the rotated and relativized line segment, expressed as UTM; along with the
   *         amount of rotation (in radians) applied to align the segment with the positive x-axis.
   */
  private def repointDirectedSegmentRightwardAndRelativize(pointA: HasLongLat, pointB: HasLongLat, referencePoint: HasLongLat): (Coordinate, Coordinate, Double) = {

    val pointA_UTM = toUTMCoordinate(pointA)
    val pointB_UTM = toUTMCoordinate(pointB)
    val directedSegmentAngleRadians = Angle.angle(pointA_UTM, pointB_UTM)  // angle the directed segment forms relative to positive x-axis

    val referencePointUTM = toUTMCoordinate(referencePoint)
    val rotationAngleRadians = -directedSegmentAngleRadians

    val pointAPrimeUTM = rotateAboutReferencePointAndRelativize(pointA_UTM, referencePointUTM, rotationAngleRadians)
    val pointBPrimeUTM = rotateAboutReferencePointAndRelativize(pointB_UTM, referencePointUTM, rotationAngleRadians)

    (pointAPrimeUTM, pointBPrimeUTM, rotationAngleRadians)
  }

  /**
   * Determine whether a directed line segment continuously nears a reference point; i.e., whether each successive point
   * on the segment is closer to the reference point than the previous point on the segment.
   *
   * @param pointA The segment's starting point.
   * @param pointB The segment's end point.
   * @param referencePoint The reference point.
   * @return Whether the segment continuously nears the reference point.
   */
  def continuouslyNears(pointA: HasLongLat, pointB: HasLongLat, referencePoint: HasLongLat): Boolean = {

    val (pointAPrimeUTM, pointBPrimeUTM, _) = repointDirectedSegmentRightwardAndRelativize(pointA, pointB, referencePoint)

    (pointAPrimeUTM.getX < 0.0) && (pointBPrimeUTM.getX <= 0.0)
  }

  /**
   * Given a reference point and a directed line segment AB that continuously nears it, calculate the point (if any)
   * that:
   *
   *   * lies on a particular halfline[1] containing AB; and
   *   * is a given distance from the reference point.
   *
   * [1] The particular halfline is the one that begins at the point where line AB passes closest to the reference
   * point. From there, the halfline extends through points B and A (in that order) to infinity.
   *
   *
   * @param pointA Directed segment AB's starting point.
   * @param pointB Directed segment AB's end point.
   * @param referencePoint The reference point.
   * @param distanceInMeters The distance in meters.
   * @throws java.lang.IllegalArgumentException If directed segment AB doesn't continuously near the reference point.
   * @return A polar angle (which, along with the passed-in distance, defines the target point), and a relative
   *         position[2], wrapped in a Some. Or, None, if line AB does not pass the reference point within the specified
   *         distance. -- [2] The relative position reflects where the target point lies on directed segment AB. If the
   *         target point matches A, the relative position is 0.0; if it is halfway from A to B, the relative position
   *         is 0.5; etc. The relative position can be < 0.0 or > 1.0.
   */
  @throws(classOf[IllegalArgumentException])
  def pointOnHalflineAtDistance(pointA: HasLongLat, pointB: HasLongLat, referencePoint: HasLongLat, distanceInMeters: Double): Option[AngleAndRelativePosition] = {

    val (pointAPrimeUTM, pointBPrimeUTM, rotationAngleRadians) = repointDirectedSegmentRightwardAndRelativize(pointA, pointB, referencePoint)

    if ((pointAPrimeUTM.getX >= 0.0) || (pointBPrimeUTM.getX > 0.0)) {
      throw new IllegalArgumentException("Directed line segment does not continuously near reference point!")
    }

    val commonYOfPointsPrimeUTM = pointAPrimeUTM.getY  // A' and B' have the same Y-coordinate, as will any C' where C is a point on the directed segment at the specified distance
    val lineAB_closestDistanceInMeters = abs(commonYOfPointsPrimeUTM)

    val xOfTargetPointPrimeUTMOption = Option.when(distanceInMeters >= lineAB_closestDistanceInMeters) {
      -sqrt(pow(distanceInMeters, 2) - pow(lineAB_closestDistanceInMeters, 2))
    }

    val targetPointOption = xOfTargetPointPrimeUTMOption.map { xOfTargetPointPrimeUTM =>
      val angleOfTargetPointRadians = Vector2D.create(xOfTargetPointPrimeUTM, commonYOfPointsPrimeUTM).rotate(-rotationAngleRadians).angle()
      val angleOfTargetPoint = PolarAngle.fromRadians(angleOfTargetPointRadians)
      val relativePosition = (xOfTargetPointPrimeUTM - pointAPrimeUTM.getX) / (pointBPrimeUTM.getX - pointAPrimeUTM.getX)
      AngleAndRelativePosition(angleOfTargetPoint, relativePosition)
    }

    targetPointOption
  }
}

object GeographicCalculator {

  /**
   * Obtain a GeographicCalculator instance.
   *
   * @param referencePoint The reference point to use in choosing an instance's UTM zone. -- Calling code should pass a
   *                       reference point in the vicinity of the coordinates that that code will eventually supply to
   *                       class methods. An approximation is generally fine, as UTM zones are quite large. (They
   *                       typically encompass six degrees of latitude and extend from the equator nearly to the North
   *                       or South Pole.)
   * @return The instance.
   */
  def apply(referencePoint: HasLongLat): GeographicCalculator = {
    val (toUTMTransform, toLongLatTransform) = toUTMAndLongLatTransforms(referencePoint)
    new GeographicCalculator(toUTMTransform, toLongLatTransform)
  }

  private def toUTMAndLongLatTransforms(referencePoint: HasLongLat): (MathTransform, MathTransform) = {
    val utmCRSCode = s"AUTO:42001,${referencePoint.longitude},${referencePoint.latitude}"
    val utmCRS = CRS.decode(utmCRSCode)
    (CRS.findMathTransform(DefaultGeographicCRS.WGS84, utmCRS), CRS.findMathTransform(utmCRS, DefaultGeographicCRS.WGS84))
  }
}
