package com.dmanchester.approachminder

import com.dmanchester.approachminder.typeswithbehavior.Trajectory
import com.dmanchester.approachminder.typeswithoutbehavior.{ModelFitWithDisplayFields, OpenSkyPositionReport}
import play.api.libs.json.*

import java.time.Instant
import java.time.format.DateTimeFormatter
import scala.math.BigDecimal.RoundingMode

object Output {
  // TODO Could we (easily) use combinator syntax in our Writes instead of what's below?

  private def scaled(value: BigDecimal, scale: Int): BigDecimal = value.setScale(scale, RoundingMode.HALF_EVEN)

  private def scaled(value: Double, scale: Int): BigDecimal = scaled(BigDecimal.valueOf(value), scale)

  private def positionToJsObject(position: OpenSkyPositionReport): JsObject = Json.obj(
    "longitude" -> position.longitude,
    "latitude" -> position.latitude,
    "altitude" -> scaled(position.altitudeMeters, 0),
    "onGround" -> position.onGround,
    "velocity" -> position.velocity.map(scaled(_, 0)),
    "trueTrack" -> position.trueTrack.map(scaled(_, 0)),
    "verticalRate" -> position.verticalRate.map(scaled(_, 1)),
    "squawk" -> position.squawk
  )

  private def modelFitToJsObject(modelFitWithDisplayFields: ModelFitWithDisplayFields) = Json.obj(
    "airport" -> modelFitWithDisplayFields.modelFit.model.runway.airport.icaoID,
    "runway" -> modelFitWithDisplayFields.modelFit.model.runway.name,
    "thresholdDistance" -> scaled(modelFitWithDisplayFields.thresholdDistanceInMeters, 0),
    "verticalDevMeters" -> scaled(modelFitWithDisplayFields.modelFit.deviation.altitudeDevInMeters, 0),
    "horizontalDevMeters" -> scaled(modelFitWithDisplayFields.horizontalDevInMeters, 0),
    "stdDevs" -> scaled(modelFitWithDisplayFields.modelFit.deviation.normalizedEuclideanDistance, 1)
  )

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

  private val trajectoriesWithModelFitsWrites: Writes[Seq[Trajectory[(OpenSkyPositionReport, Option[ModelFitWithDisplayFields])]]] = Writes.seq(trajectoryWithModelFitsWrites)

  /**
   * Produce JSON for a sequence of trajectories. The trajectories' positions are expected to be a 2-tuple with an
   * OpenSkyPositionReport in the first position.
   *
   * In the second position, each tuple can optionally have a ModelFitWithDisplayFields (from having fit the trajectory
   * to a model).
   *
   * @param trajectories The trajectories.
   * @return The JSON.
   */
  def openSkyTrajectoriesToJson(trajectories: Seq[Trajectory[(OpenSkyPositionReport, Option[ModelFitWithDisplayFields])]]): String = {
    val trajectoriesJson = Json.toJson(trajectories)(trajectoriesWithModelFitsWrites)
    trajectoriesJson.toString()
  }
}
