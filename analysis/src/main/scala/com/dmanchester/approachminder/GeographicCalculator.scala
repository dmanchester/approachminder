package com.dmanchester.approachminder

import org.geotools.geometry.jts.{JTS, JTSFactoryFinder}
import org.geotools.measure.Units
import org.geotools.referencing.CRS
import org.geotools.referencing.crs.DefaultGeographicCRS
import org.locationtech.jts.algorithm.Angle
import org.locationtech.jts.geom.{Coordinate, LineSegment, Point}
import org.locationtech.jts.math.Vector2D
import org.opengis.referencing.operation.MathTransform

import scala.math.{abs, pow, sqrt}

class GeographicCalculator private(val referencePoint: HasLongLat, private val toUTMTransform: MathTransform, private val toLongLatTransform: MathTransform) {

  def distanceInMeters(point0: HasLongLat, point1: HasLongLat): Double = {

    val point0UTM = toUTMCoordinate(point0)
    val point1UTM = toUTMCoordinate(point1)

    point0UTM.distance(point1UTM)
  }

  /**
   * Calculate the angle from an origin to a point.
   *
   * @param point
   * @param origin
   * @return
   */
  def angle(origin: HasLongLat, point: HasLongLat): PolarAngle = {

    val originUTM = toUTMCoordinate(origin)
    val pointBUTM = toUTMCoordinate(point)

    PolarAngle.fromRadians(Angle.angle(originUTM, pointBUTM))
  }

  /**
   * Calculate the point that lies at a given polar angle and distance from an origin.
   *
   * TODO Need tests
   *
   * @param origin
   * @param angle
   * @param distanceMeters
   * @return
   */
  def pointAtAngleAndDistance(origin: HasLongLat, angle: PolarAngle, distanceMeters: Double): HasLongLat = {

    val originUTM = toUTMCoordinate(origin)
    val originVectorUTM = Vector2D.create(originUTM)

    val thePointVectorUTM = Vector2D.create(distanceMeters, 0).rotate(angle.toRadians).add(originVectorUTM)

    toLongLat(thePointVectorUTM.toCoordinate)
  }

  private def toUTMCoordinate(longLat: HasLongLat): Coordinate = {
    // TODO This method, or toUTMPoint, would be a prime candidate for caching. Can't do equality comparisons (because Double), but can check for reference equality.
    //
    // Could also have an option "cacheable" attribute on HasLongLat, but I don't like to increase the complexity of the API.
    //
    // In any case, a map of weak references.
    //
    // A big question--in this case, and in other cases of potential caching--is what to cache: JTS Point vs. JTS Coordinate vs. an immutable object of UTM coordinates. One of the first two would be most performant, but do we end up reusing mutable data structures? Yuck, risky.
    toUTMPoint(longLat).getCoordinate()
  }

  private def toUTMPoint(longLat: HasLongLat): Point = {
    val longLatAsPoint: Point = geometryFactory.createPoint(new Coordinate(longLat.longitude, longLat.latitude))
    JTS.transform(longLatAsPoint, toUTMTransform).asInstanceOf[Point]
  }

  private def geometryFactory = JTSFactoryFinder.getGeometryFactory()

  /**
   * Determine whether two line segments intersect. If they do, return the point of intersection, as
   * well as where the intersection lies along segment #0, expressed as a percentage of the
   * segment's total length as measured from its first point.
   *
   * @param lineSegment0
   * @param lineSegment1
   * @return
   */
  def intersection(lineSegment0: (HasLongLat, HasLongLat), lineSegment1: (HasLongLat, HasLongLat)): Option[(HasLongLat, Double)] = {

    // TODO Introduce type for this return type; use it here and elsewhere?

    val lineSegment0UTM = toUTMLineSegment(lineSegment0)
    val lineSegment1UTM = toUTMLineSegment(lineSegment1)

    val intersectionOrNullUTM = lineSegment0UTM.intersection(lineSegment1UTM)

    Option(intersectionOrNullUTM).map { intersectionUTM =>
      val intersectionLongLat = toLongLat(intersectionUTM)
      val percentageFromSeg0StartToSeg0End = lineSegment0UTM.p0.distance(intersectionUTM) / lineSegment0UTM.p0.distance(lineSegment0UTM.p1)
      (intersectionLongLat, percentageFromSeg0StartToSeg0End)
    }
  }

  private def toUTMLineSegment(lineSegment: (HasLongLat, HasLongLat)): LineSegment = {

    val aCoordinateUTM = toUTMCoordinate(lineSegment._1)
    val bCoordinateUTM = toUTMCoordinate(lineSegment._2)

    new LineSegment(aCoordinateUTM, bCoordinateUTM)
  }

  private def toLongLat(utmCoordinate: Coordinate): HasLongLat = {
    val utmCoordinateAsPoint = geometryFactory.createPoint(utmCoordinate)
    val longLatAsCoordinate = JTS.transform(utmCoordinateAsPoint, toLongLatTransform).asInstanceOf[Point].getCoordinate

    LongLat(longLatAsCoordinate.x, longLatAsCoordinate.y)
  }

  def contains(polygon: Polygon, point: HasLongLat) = {

    // TODO In name of performance, do some basic check based on longitude and latitude before bothering with the projection-based stuff?

    val perimeterUTM = polygon.perimeter.map(toUTMCoordinate)
    val perimeterClosedUTM = perimeterUTM :+ perimeterUTM.head // make the final vertex the first one, closing the geometry

    val polygonUTM = geometryFactory.createPolygon(perimeterClosedUTM.toArray)

    polygonUTM.contains(toUTMPoint(point))
  }

  /**
   * Rotate a point about an arbitrary origin and scale its position to a
   * pre-determined distance from that origin.
   *
   * @param point
   * @param arbitraryOrigin
   * @param rotationInDegrees positive value = counterclockwise
   * @param distanceInMeters
   * @return
   */
  def rotateAboutArbitraryOriginAndScaleToDistance(point: HasLongLat, arbitraryOrigin: HasLongLat, rotationInDegrees: Double, distanceInMeters: Double): HasLongLat = {

    // TODO Drop "arbitrary" throughout naming?

    val arbitraryOriginUTM = toUTMCoordinate(arbitraryOrigin)
    val pointUTM = toUTMCoordinate(point)

    val degreesToRadiansConverter = Units.DEGREE_ANGLE.getConverterTo(Units.RADIAN)
    val rotationInRadians = degreesToRadiansConverter.convert(rotationInDegrees)

    val distanceInMetersBeforeScaling = arbitraryOriginUTM.distance(pointUTM)
    val scaleFactor = distanceInMeters / distanceInMetersBeforeScaling

    val arbitraryOriginVectorUTM = new Vector2D(arbitraryOriginUTM) // FIXME Flip to static "create"? What about other uses of JTS "new"?

    val transformedPointVectorUTM = new Vector2D(pointUTM).subtract(arbitraryOriginVectorUTM).rotate(rotationInRadians).multiply(scaleFactor).add(arbitraryOriginVectorUTM)

    toLongLat(transformedPointVectorUTM.toCoordinate)
  }

  /**
   * Determine the point on a halfline, if any, that is a given distance from a reference point.
   *
   * Given points A and B, along with a reference point, this method views the line containing
   * A and B as two halflines about the line's point closest to the reference point.
   *
   * The halfline in question is the one containing A and B.
   *
   * This method is intended primarily for the case that directed line segment AB continuously nears
   * the reference point and that A and B reside on the same halfline on that basis.
   *
   * This method's output is *undefined* for the case that A and B reside on opposite halflines.
   *
   * This method's primary output is a polar angle. That polar angle and the passed-in distance
   * define the target point.
   *
   * This method's secondary output is the relative position of the target point on directed line
   * segment AB. If the target point matches A, the relative position is 0.0; if it is halfway from
   * A to B, the relative position is 0.5; etc.
   *
   * The relative position can be < 0.0 or > 1.0.
   *
   * @param approachingLineSegment
   * @param referencePoint
   * @param distanceInMeters
   * @return
   */
  def pointOnHalflineAtDistance(pointA: HasLongLat, pointB: HasLongLat, referencePoint: HasLongLat, distanceInMeters: Double): Option[PolarAngleAndRelativePosition] = {

    val pointAUTM = toUTMCoordinate(pointA)
    val pointBUTM = toUTMCoordinate(pointB)
    val directedLineSegmentAngleRadians = Angle.angle(pointAUTM, pointBUTM)  // angle is relative to positive x-axis

    val vectorToReferencePointUTM = Vector2D.create(toUTMCoordinate(referencePoint))

    val vectorToPointAPrimeUTM = Vector2D.create(pointAUTM).subtract(vectorToReferencePointUTM).rotate(-directedLineSegmentAngleRadians)

    val commonYOfPointsPrimeUTM = vectorToPointAPrimeUTM.getY  // A' and B' have the same Y-coordinate, as will any C' where C is a point on the directed line segment at the specified distance
    val containingLineClosestDistanceInMeters = abs(commonYOfPointsPrimeUTM)

    val xOfTargetPointPrimeUTMOption: Option[Double] = if (distanceInMeters < containingLineClosestDistanceInMeters) {
      None
    } else if (distanceInMeters > containingLineClosestDistanceInMeters) {
      Some(-sqrt(pow(distanceInMeters, 2) - pow(containingLineClosestDistanceInMeters, 2)))
    } else { // distanceInMeters == containingLineClosestDistanceInMeters
      Some(0.0)
    }

    val targetPointOption = xOfTargetPointPrimeUTMOption.map { xOfTargetPointPrimeUTM =>
      val angleOfTargetPointRadians = Vector2D.create(xOfTargetPointPrimeUTM, commonYOfPointsPrimeUTM).rotate(directedLineSegmentAngleRadians).angle()
      val angleOfTargetPoint = PolarAngle.fromRadians(angleOfTargetPointRadians)
      val vectorToPointBPrimeUTM = Vector2D.create(pointBUTM).subtract(vectorToReferencePointUTM).rotate(-directedLineSegmentAngleRadians)
      val percentageFromAToBOfTargetPoint = (xOfTargetPointPrimeUTM - vectorToPointAPrimeUTM.getX) / (vectorToPointBPrimeUTM.getX - vectorToPointAPrimeUTM.getX)
      PolarAngleAndRelativePosition(angleOfTargetPoint, percentageFromAToBOfTargetPoint)
    }

    targetPointOption
  }

  // TODO Document!
  def pointOnContinuouslyNearingSegmentAtDistance(pointA: HasLongLat, pointB: HasLongLat, referencePoint: HasLongLat, distanceInMeters: Double): Option[PolarAngleAndRelativePosition] = {

    val targetPointOption = pointOnHalflineAtDistance(pointA, pointB, referencePoint, distanceInMeters)

    targetPointOption.filter { targetPoint =>
      0.0 <= targetPoint.relativePosition && targetPoint.relativePosition <= 1.0
    }
  }

  // TODO Establish ContinuouslyNearingSegment; have a pseudo-constructor that packages the
  //  continuouslyNears innards below and  returns Option[...]. -- Then the two pointOn... methods
  //  can become instance methods of that class. -- How does this plan mesh with
  //  CountinuouslyNearingTrajectory? -- NOTE: While the preceding may be worthwhile, *aborted* a
  //  first attempt at it once it became clear most of the innards would need to remain here, due to
  //  reliance on JTS primitives.

  def continuouslyNears(pointA: HasLongLat, pointB: HasLongLat, referencePoint: HasLongLat): Boolean = {

    // FIXME How to best factor out code shared w/ above method?

    val pointAUTM = toUTMCoordinate(pointA)
    val pointBUTM = toUTMCoordinate(pointB)
    val directedLineSegmentAngleRadians = Angle.angle(pointAUTM, pointBUTM) // angle is relative to positive x-axis

    val vectorToReferencePointUTM = Vector2D.create(toUTMCoordinate(referencePoint))

    val vectorToPointAPrimeUTM = Vector2D.create(pointAUTM).subtract(vectorToReferencePointUTM).rotate(-directedLineSegmentAngleRadians)

    (vectorToPointAPrimeUTM.getX < 0.0) && // Following line includes calculation of vectorToPointBPrimeUTM. We do it in this fashion to avoid calculating it unnecessarily if first term is false.
      (Vector2D.create(pointBUTM).subtract(vectorToReferencePointUTM).rotate(-directedLineSegmentAngleRadians).getX <= 0.0)
  }

  def pointOnSegment(lineSegment: (HasLongLat, HasLongLat), relativePosition: Double): HasLongLat = {
    val lineSegmentUTM = toUTMLineSegment(lineSegment)
    val point = lineSegmentUTM.pointAlong(relativePosition)
    toLongLat(point)
  }
}

object GeographicCalculator {

  def apply(referencePoint: HasLongLat) = {
    val (toUTMTransform, toLongLatTransform) = toUTMAndToLongLatTransforms(referencePoint)
    new GeographicCalculator(referencePoint, toUTMTransform, toLongLatTransform)
  }

  private def toUTMAndToLongLatTransforms(referencePoint: HasLongLat): (MathTransform, MathTransform) = {

    val crsCodeUTM = s"AUTO:42001,${referencePoint.longitude},${referencePoint.latitude}"

    val utmCRS = CRS.decode(crsCodeUTM)

    (CRS.findMathTransform(DefaultGeographicCRS.WGS84, utmCRS), CRS.findMathTransform(utmCRS, DefaultGeographicCRS.WGS84))
  }
}
