package com.dmanchester.approachminder

import com.dmanchester.approachminder.typeswithbehavior.Trajectory
import com.dmanchester.approachminder.typeswithoutbehavior.{HasLongLat, ModelFitWithDisplayFields, OpenSkyPositionReport}
import play.api.libs.json.*

import java.time.Instant
import java.time.format.DateTimeFormatter
import scala.math.BigDecimal.RoundingMode

object Output {
  // TODO Could we (easily) use combinator syntax in our Writes instead of what's below?

  private def scaled(value: BigDecimal, scale: Int): BigDecimal = value.setScale(scale, RoundingMode.HALF_EVEN)

  private def scaled(value: Double, scale: Int): BigDecimal = scaled(BigDecimal.valueOf(value), scale)

  private def positionToJsObject(position: OpenSkyPositionReport): JsObject = {
    Json.obj(
      // TODO Set scale on any of these values?
      "longitude" -> position.longitude,
      "latitude" -> position.latitude,
      "altitude" -> scaled(position.altitudeMeters, 0),
      "onGround" -> position.onGround,
      "velocity" -> position.velocity.map(scaled(_, 0)),
      "trueTrack" -> position.trueTrack.map(scaled(_, 0)),
      "verticalRate" -> position.verticalRate.map(scaled(_, 1)),
      "squawk" -> position.squawk
    )
  }

  private def modelFitToJsObject(modelFitWithDisplayFields: ModelFitWithDisplayFields) = {
    Json.obj(
      "airport" -> modelFitWithDisplayFields.modelFit.model.runway.airport.icaoID,
      "runway" -> modelFitWithDisplayFields.modelFit.model.runway.name,
      "thresholdDistance" -> scaled(modelFitWithDisplayFields.thresholdDistanceInMeters, 0),
      "verticalDevMeters" -> scaled(modelFitWithDisplayFields.modelFit.deviation.altitudeDevInMeters, 0),
      "horizontalDevMeters" -> scaled(modelFitWithDisplayFields.horizontalDevInMeters, 0),
      "stdDevs" -> scaled(modelFitWithDisplayFields.modelFit.deviation.normalizedEuclideanDistance, 1)
    )
  }

  private val positionWithModelFitWrites = new Writes[(OpenSkyPositionReport, Option[ModelFitWithDisplayFields])] {

    override def writes(positionWithModelFit: (OpenSkyPositionReport, Option[ModelFitWithDisplayFields])): JsValue = {
      positionToJsObject(positionWithModelFit._1) + (
        "modelFit" -> positionWithModelFit._2.map(modelFitToJsObject).getOrElse(JsNull) // TODO Seems suboptimal to have to be explicit about null-handling
      )
    }
  }

  private val positionsWithModelFitsWrites = new Writes[Seq[(OpenSkyPositionReport, Option[ModelFitWithDisplayFields])]] {

    override def writes(positionsWithModelFits: Seq[(OpenSkyPositionReport, Option[ModelFitWithDisplayFields])]): JsValue = {

      JsObject( positionsWithModelFits.map { positionWithModelFit =>

        val instant = Instant.ofEpochSecond(positionWithModelFit._1.timePosition.toLong)
        val formattedInstant = DateTimeFormatter.ISO_INSTANT.format(instant)

        formattedInstant -> positionWithModelFitWrites.writes(positionWithModelFit)  // TODO Is this "right"/optimal? Could use combinator syntax instead?
      })
    }
  }

  private val trajectoryWithModelFitsWrites = new Writes[Trajectory[(OpenSkyPositionReport, Option[ModelFitWithDisplayFields])]] {

    override def writes(trajectoryWithModelFits: Trajectory[(OpenSkyPositionReport, Option[ModelFitWithDisplayFields])]): JsValue = {

      Json.obj(
        "icao24" -> trajectoryWithModelFits.icao24,
        "callsign" -> trajectoryWithModelFits.callsign,  // TODO What does this output in "None" case? -- Also, may be relying on default Some.toString, which seems sub-optimal
        "category" -> trajectoryWithModelFits.category.map(_.getClass.getSimpleName),  // TODO Switch to a user-friendly category descriptor
        "positions" -> positionsWithModelFitsWrites.writes(trajectoryWithModelFits.positions)  // TODO Is this "right"/optimal? Could use combinator syntax instead?
      )
    }
  }

  val trajectoriesWithModelFitsWrites: Writes[Seq[Trajectory[(OpenSkyPositionReport, Option[ModelFitWithDisplayFields])]]] = Writes.seq(trajectoryWithModelFitsWrites)

// ----------------------------------------------

  def timeBasedPositionPartiallyToJsObject(timeBasedPosition: TimeBasedPosition): JsObject = {  // TODO Make method private?
    Json.obj(
      // TODO Set scale on any of these values and/or reach "into" vector for the BigDecimals (although they're Options)?
      "longitude" -> timeBasedPosition.longitude,
      "latitude" -> timeBasedPosition.latitude,
      "altitude" -> setScale(timeBasedPosition.altitudeMeters, 0),  // TODO Include units in JSON field name? (Below, too?)
      "onGround" -> timeBasedPosition.vector.onGround,
      "velocity" -> timeBasedPosition.vector.velocity.map(setScale(_, 0)), // Option
      "trueTrack" -> timeBasedPosition.vector.trueTrack.map(setScale(_, 0)), // Option
      "verticalRate" -> timeBasedPosition.vector.verticalRate.map(setScale(_, 1)), // Option
      "squawk" -> timeBasedPosition.vector.squawk // Option
    )
  }

  val timeBasedPositionPartialWrites = new Writes[TimeBasedPosition] { // TODO Is this wrapping of timeBasedPositionToJsObject necessary? In general, only bother with a Writes if we're using combinators or convenience methods?
    override def writes(timeBasedPosition: TimeBasedPosition): JsValue = {
      timeBasedPositionPartiallyToJsObject(timeBasedPosition)
    }
  }

  def setScale(bigDecimal: BigDecimal, scale: Int): BigDecimal = bigDecimal.setScale(scale, RoundingMode.HALF_EVEN)  // TODO Make private? Conversely, move to MathUtils? (Same for next one.)

  def setScale(double: Double, scale: Int): BigDecimal = setScale(BigDecimal.valueOf(double), scale)

  private def approachSegmentWithDeviationToJsObject(approachSegmentWithDeviation: ApproachSegmentWithDeviation) = {
    Json.obj(
      "airport" -> approachSegmentWithDeviation.runway.airport.icaoID,
      "threshold" -> approachSegmentWithDeviation.runway.name,
      "thresholdDistanceMeters" -> setScale(approachSegmentWithDeviation.thresholdDistanceMeters, 0),
      "verticalDevMeters" -> setScale(approachSegmentWithDeviation.verticalDevMeters, 0),
      "horizontalDevMeters" -> setScale(approachSegmentWithDeviation.horizontalDevMeters, 0),
      "normalizedEuclideanDistance" -> setScale(approachSegmentWithDeviation.normalizedEuclideanDistance, 1)
    )
  }

  val positionWithApproachSegmentWrites = new Writes[TimeBasedPositionWithApproachSegment] {

    override def writes(timeBasedPositionWithApproachSegment: TimeBasedPositionWithApproachSegment): JsValue = {
      timeBasedPositionPartiallyToJsObject(timeBasedPositionWithApproachSegment.timeBasedPosition) + (
        "approachSegment" -> timeBasedPositionWithApproachSegment.approachSegment.map(approachSegmentWithDeviationToJsObject).getOrElse(JsNull) // TODO Seems suboptimal to have to be explicity about null-handling
      )
    }
  }

  val multiplePositionWithApproachSegmentWrites = new Writes[Seq[TimeBasedPositionWithApproachSegment]] {

    override def writes(positionsWithApproachSegments: Seq[TimeBasedPositionWithApproachSegment]): JsValue = {

      JsObject( positionsWithApproachSegments.map { positionWithApproachSegment =>

        val instant = Instant.ofEpochSecond(positionWithApproachSegment.timeBasedPosition.timePosition.toLong)
        val formattedInstant = DateTimeFormatter.ISO_INSTANT.format(instant)

        formattedInstant -> positionWithApproachSegmentWrites.writes(positionWithApproachSegment)  // TODO Is this "right"/optimal? Could use combinator syntax instead?
      })
    }
  }

  def toWKT(trajectory: Seq[HasLongLat]): String = {
    val contents = trajectory.map({ point => s"${point.longitude} ${point.latitude}" }).mkString(", ")
    s"LINESTRING ($contents)"
  }

//  import scala.xml.XML
//
//    val kml = <kml xmlns="http://www.opengis.net/kml/2.2">
//    <Document>
//      <name>DPMDocumentName.kml</name>
//      <open>1</open>{trajectories.map(trajectory => {
//      <Placemark>
//        <name>
//          {trajectory._1}
//        </name>
//        <LineString>
//          <altitudeMode>absolute</altitudeMode>
//          <coordinates>
//            {trajectory._2.map(tuple => s"${tuple._2.longitude},${tuple._2.latitude},${tuple._2.altitude}").mkString(" ")}
//          </coordinates>
//        </LineString>
//      </Placemark>
//    })}
//    </Document>
//  </kml>
//
//  val fileWriter = new FileWriter("/tmp/2022-10-17.kml", false)
//  XML.write(fileWriter, kml, "utf-8", true /* xmlDecl */ , null /* doctype */)
//  fileWriter.close()


//  private val angleParser = new AngleFormat("D°M'S.s\"") // TODO Confirm threadsafe, OK to reuse
//
//  def angleStringToDegrees(angle: String): Double = { // TODO Come up with more-exacting name
//    return angleParser.parse(angle).degrees()
//  }


  //  def convertVectorsToTrajectory(vectors: UniqueVectors): Seq[LongLatWithStateVector] = {
//
//    val sortedVectors = vectors.seq.sortBy(_.timePosition.get)  // TODO Regarding "timePosition.get", we previously confirmed these Options have values, but it'd still be nice to avoid ".get"
//    val trajectoryPotentialTimeConflicts = sortedVectors.map(LongLatWithStateVector(_))
//
//    val trajectory = trajectoryPotentialTimeConflicts.sliding(2).foldLeft(Seq.empty[LongLatWithStateVector]) {(trajectoryInProgress, twoVectors) =>
//
//      // TODO Definitiely gotta document what this is about
//      if ((twoVectors.length == 1  /* last element */) || (twoVectors(0).timePosition != twoVectors(1).timePosition)) {
//        trajectoryInProgress :+ twoVectors(0)
//      } else {
//        trajectoryInProgress
//      }
//    }
//
////    val vectorsEliminated = trajectoryPotentialTimeConflicts.length - trajectory.length
////    if (vectorsEliminated > 0) {
////      println(s"convertVectorsToTrajectory eliminated $vectorsEliminated vectors.")
////    }
//
//    trajectory
//  }
}
