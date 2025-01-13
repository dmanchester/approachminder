package com.dmanchester.approachminder

import com.dmanchester.approachminder.AIXM.{AIXMAirportHeliport, AIXMRunway, AIXMRunwayDirection}
import com.dmanchester.approachminder.AIXMRepository.{AirportHeliportGmlId, RunwayGmlId, RunwayGmlIdNumericPortion, gmlIdNonNumericAndNumericPortions}

import scala.collection.immutable.Map

case class AIXMRepository private(runwaysMain: Map[AirportHeliportGmlId, Seq[AIXMRunway]], runwaysBaseEnd: Map[RunwayGmlIdNumericPortion, AIXMRunway], runwaysReciprocalEnd: Map[RunwayGmlIdNumericPortion, AIXMRunway], runwayDirections: Map[RunwayGmlId, AIXMRunwayDirection]) {

  def printAirportDetails(airportHeliportGmlId: AirportHeliportGmlId): Unit = {

    println()

    val theRunwaysMain = runwaysMain(airportHeliportGmlId)
    theRunwaysMain.foreach { runwayMain =>
      val (_, runwayGmlIdNumericPortion) = gmlIdNonNumericAndNumericPortions(runwayMain.gmlId)
      val runwayBaseEnd = runwaysBaseEnd(runwayGmlIdNumericPortion)
      val runwayDirectionBaseEnd = runwayDirections(runwayBaseEnd.gmlId)
      val runwayReciprocalEnd = runwaysReciprocalEnd(runwayGmlIdNumericPortion)
      val runwayDirectionReciprocalEnd = runwayDirections(runwayReciprocalEnd.gmlId)

      println(s"Runway surface ${runwayMain.designator} is ${runwayMain.widthStrip.get.value} ${runwayMain.widthStrip.get.uom} wide.")
      println(s"  Runway ${runwayBaseEnd.designator}'s threshold is centered at ${runwayDirectionBaseEnd.runwayEnd.get}.")
      println(s"  Runway ${runwayReciprocalEnd.designator}'s threshold is centered at ${runwayDirectionReciprocalEnd.runwayEnd.get}.")
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
