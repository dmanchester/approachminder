package com.dmanchester.approachminder

import com.dmanchester.approachminder.AIXM.{AIXMAirportHeliport, AIXMLongLat, AIXMRunway, AIXMRunwayDirection}
import com.dmanchester.approachminder.AIXMRepository.{AirportHeliportGmlId, RunwayGmlId, RunwayGmlIdNumericPortion, gmlIdNonNumericAndNumericPortions}
import com.dmanchester.approachminder.Utils.feetToMetersConverter

import scala.collection.immutable.Map

case class AIXMRepository private(runwaysMain: Map[AirportHeliportGmlId, Seq[AIXMRunway]], runwaysBaseEnd: Map[RunwayGmlIdNumericPortion, AIXMRunway], runwaysReciprocalEnd: Map[RunwayGmlIdNumericPortion, AIXMRunway], runwayDirections: Map[RunwayGmlId, AIXMRunwayDirection]) {

  val expectedUom = "FT"

  private def toLongLat(aixmLongLat: AIXMLongLat): LongLat = {
    LongLat.apply(aixmLongLat.longitude.toDouble, aixmLongLat.latitude.toDouble)
  }

  def getRunwaySurfaceTemplate(runwayMain: AIXMRunway): Either[String, RunwaySurfaceTemplate] = {

    for {
      runwayMainWidthStrip <- runwayMain.widthStrip.filter(_.uom == expectedUom).toRight(s"Main runway does not include a width strip with UOM '$expectedUom'.")

      (_, runwayGmlIdNumericPortion) = gmlIdNonNumericAndNumericPortions(runwayMain.gmlId)

      runwayBaseEnd <- runwaysBaseEnd.get(runwayGmlIdNumericPortion).toRight(s"No base-end runway found whose GML ID has numeric portion '$runwayGmlIdNumericPortion'.")
      runwayDirectionBaseEnd <- runwayDirections.get(runwayBaseEnd.gmlId).toRight(s"No runway direction found for base-end runway with GML ID '${runwayBaseEnd.gmlId}'.")
      runwayDirectionBaseEndPoint <- runwayDirectionBaseEnd.runwayEnd.toRight("No point found for base-end runway direction.")

      runwayReciprocalEnd <- runwaysReciprocalEnd.get(runwayGmlIdNumericPortion).toRight(s"No reciprocal-end runway found whose GML ID has numeric portion '$runwayGmlIdNumericPortion'.")
      runwayDirectionReciprocalEnd <- runwayDirections.get(runwayReciprocalEnd.gmlId).toRight(s"No runway direction found for reciprocal-end runway with GML ID '${runwayReciprocalEnd.gmlId}'.")
      runwayDirectionReciprocalEndPoint <- runwayDirectionReciprocalEnd.runwayEnd.toRight("No point found for reciprocal-end runway direction.")

      widthInMeters = feetToMetersConverter.convert(runwayMainWidthStrip.value)
    } yield {
      RunwaySurfaceTemplate(widthInMeters, runwayBaseEnd.designator, toLongLat(runwayDirectionBaseEndPoint), runwayReciprocalEnd.designator, toLongLat(runwayDirectionReciprocalEndPoint))
    }
  }

  def printAirportDetails(airportHeliportGmlId: AirportHeliportGmlId): Unit = {

    val theRunwaysMain = runwaysMain(airportHeliportGmlId)
    theRunwaysMain.foreach { runwayMain =>
      val runwaySurfaceTemplate = getRunwaySurfaceTemplate(runwayMain)
      println(s"  Runway surface ${runwayMain.designator}: $runwaySurfaceTemplate")
    }
  }
}

object AIXMRepository {

  type AirportHeliportGmlId = String
  type RunwayGmlId = String
  type RunwayGmlIdNumericPortion = String

  private val gmlIdSplitter = "([A-Z_]+)_([0-9_]+)".r

  private def gmlIdNonNumericAndNumericPortions(gmlId: String): (String, String) = {
    gmlId match {
      case gmlIdSplitter(nonNumericPortion, numericPortion) => (nonNumericPortion, numericPortion)
    }
  }

  def apply(theAirportHeliports: Seq[AIXMAirportHeliport], theRunways: Seq[AIXMRunway], theRunwayDirections: Seq[AIXMRunwayDirection]): AIXMRepository = {

    val (runwaysMainAsSeq, runwaysBaseEnd, runwaysReciprocalEnd) = theRunways.foldLeft((Seq.empty[AIXMRunway], Map.empty[RunwayGmlIdNumericPortion, AIXMRunway], Map.empty[RunwayGmlIdNumericPortion, AIXMRunway])) { case ((mainInProgress, baseEndInProgress, reciprocalEndInProgress), runway) =>

      val (runwayGmlIdNonNumericPortion, runwayGmlIdNumericPortion) = gmlIdNonNumericAndNumericPortions(runway.gmlId)

      runwayGmlIdNonNumericPortion match {
        case "RWY" => (mainInProgress :+ runway, baseEndInProgress, reciprocalEndInProgress)
        case "RWY_BASE_END" => (mainInProgress, baseEndInProgress.updated(runwayGmlIdNumericPortion, runway), reciprocalEndInProgress)
        case "RWY_RECIPROCAL_END" => (mainInProgress, baseEndInProgress, reciprocalEndInProgress.updated(runwayGmlIdNumericPortion, runway))
      }
    }

    val runwaysMain = runwaysMainAsSeq.groupBy(_.associatedAirportHeliportGmlId)

    val runwayDirections = theRunwayDirections.foldLeft(Map.empty[RunwayGmlId, AIXMRunwayDirection]) { case (directionsInProgress, runwayDirection) =>
      directionsInProgress.updated(runwayDirection.usedRunwayGmlId, runwayDirection)  // TODO Switch to updatedWith, throw if a key already exists?
    }

    new AIXMRepository(runwaysMain, runwaysBaseEnd, runwaysReciprocalEnd, runwayDirections)
  }
}
