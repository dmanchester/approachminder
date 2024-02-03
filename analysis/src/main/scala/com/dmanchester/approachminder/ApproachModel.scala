package com.dmanchester.approachminder

import scala.math.abs

class ApproachModel private(thresholdCenter: HasLongLat, distributionsByDistanceInMeters: Map[BigDecimal, PositionDistribution], calculator: GeographicCalculator) {

  private val distributionDistancesInMeters = Intervals.fromPointsSet(distributionsByDistanceInMeters.keySet)  // TODO Does "private val" enforce same (lack of) visibility as constructor params?
  val minDistanceInMeters = distributionDistancesInMeters.min
  val maxDistanceInMeters = distributionDistancesInMeters.max

  private def calcWithinRange(pointToTestWithAltitude: AngleAndAltitude, distanceToTestAt: BigDecimal): WithinRange = {

    val distribution = distributionsByDistanceInMeters(distanceToTestAt)
    val deviation = distribution.deviation(pointToTestWithAltitude)

    WithinRange(distanceToTestAt, deviation)
  }

  def test(previousPoint: HasLongLatAlt, currentPoint: HasLongLatAlt): ApproachModelTestResult = {

    if (calculator.continuouslyNears(previousPoint, currentPoint, thresholdCenter)) {

      val distanceToThresholdInMeters = calculator.distanceInMeters(currentPoint, thresholdCenter)
      val distanceToThresholdInMetersAsBD = BigDecimal.decimal(distanceToThresholdInMeters)

      distributionDistancesInMeters.search(distanceToThresholdInMetersAsBD) match {

        case GreaterThanMax => ContinuouslyNearingButOutOfRange

        case BetweenPoints(nextModelDistanceInMeters, prevModelDistanceInMeters) => {  // TODO Document why next, *then* prev; def. confusing!

          val possiblePointsToTestWithDistance = Set(prevModelDistanceInMeters, nextModelDistanceInMeters).flatMap { distance =>
            // TODO Confirm, conceptually, that the flatMap is needed. (Seeks to address the case where currentPoint is
            //  between two model distances, but line of previousPoint,currentPoint never gets that close.) -- Then,
            //  document and add tests.
            val point = calculator.pointOnHalflineAtDistance(previousPoint, currentPoint, thresholdCenter, distance.toDouble)
            point.map((_, distance))
          }

          val (pointToTest, distanceToTestAt) = possiblePointsToTestWithDistance.minBy { pointWithDistance =>
            val point = pointWithDistance._1
            abs(point.relativePosition - 1.0)
          }

          val altitudeToTest = MathUtils.interpolateScalar(previousPoint.altitudeMeters, currentPoint.altitudeMeters, pointToTest.relativePosition)
          val pointToTestWithAltitude = AngleAndAltitude(pointToTest.angle, altitudeToTest)

          calcWithinRange(pointToTestWithAltitude, distanceToTestAt)
        }

        case LessThanMin => {

          val distanceToTestAt = minDistanceInMeters
          val pointToTest = calculator.pointOnHalflineAtDistance(previousPoint, currentPoint, thresholdCenter, distanceToTestAt.toDouble).get

          val altitudeToTest = MathUtils.interpolateScalar(previousPoint.altitudeMeters, currentPoint.altitudeMeters, pointToTest.relativePosition)
          val pointToTestWithAltitude = AngleAndAltitude(pointToTest.angle, altitudeToTest)

          calcWithinRange(pointToTestWithAltitude, distanceToTestAt)
        }

        case MatchesAPoint => { // TODO Note how exceptionally rare this is
          // FIXME Haven't yet tested
          val pointToTestAngleOnly = calculator.angle(thresholdCenter, currentPoint)
          val distanceToTestAt = distanceToThresholdInMetersAsBD

          val altitudeToTest = currentPoint.altitudeMeters
          val pointToTestWithAltitude = AngleAndAltitude(pointToTestAngleOnly, altitudeToTest)

          calcWithinRange(pointToTestWithAltitude, distanceToTestAt)
        }
      }
    } else {
      NotContinuouslyNearing
    }
  }
}

object ApproachModel {

  def newOption(thresholdCenter: HasLongLat, distributionsByDistanceInMeters: Map[BigDecimal, PositionDistribution], calculator: GeographicCalculator): Option[ApproachModel] = {
    // TODO Is this Option-returning constructor tedious when we're just creating isolated objects (as opposed to
    //  mapping over Seqs etc.)? -- Get rid of, but throw exception on empty input?
    Option.when(!distributionsByDistanceInMeters.isEmpty) {
      new ApproachModel(thresholdCenter, distributionsByDistanceInMeters, calculator: GeographicCalculator)
    }
  }
}

sealed trait ApproachModelTestResult

case object NotContinuouslyNearing extends ApproachModelTestResult

case object ContinuouslyNearingButOutOfRange extends ApproachModelTestResult

case class WithinRange(val appliedDistributionInMeters: BigDecimal, val deviation: DeviationFromPositionDistribution) extends ApproachModelTestResult
