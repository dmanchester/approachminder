package com.dmanchester.approachminder

import play.api.libs.functional.syntax.{toFunctionalBuilderOps, unlift}
import play.api.libs.json.Reads._
import play.api.libs.json._

import java.nio.file.{Files, Path}
import java.time.Instant
import java.time.format.DateTimeFormatter
import scala.jdk.CollectionConverters._
import scala.math.BigDecimal.RoundingMode
import scala.util.{Failure, Success, Try, Using}

object IO {

  def resolveGlob(dir: Path, glob: String): Seq[Path] = {
    Using.resource(Files.newDirectoryStream(dir, glob)) { (dirStreamAsJavaIterable: java.lang.Iterable[Path]) =>
      dirStreamAsJavaIterable.asScala.toSeq
    }
  }

  private val stateVectorReads: Reads[OpenSkyVector] = (
    (JsPath \ 0).read[String] and
      // Callsigns (see next line) have trailing whitespace. (Interestingly, other String vector
      // fields do not.)
      (JsPath \ 1).readNullable[String].map(_.map(_.trim)) and // TODO Cleaner way to write the double "map" (first one gets Option[String]; second one gets String)?
      (JsPath \ 2).read[String] and
      (JsPath \ 3).readNullable[BigInt] and
      (JsPath \ 4).read[BigInt] and
      (JsPath \ 5).readNullable[BigDecimal] and
      (JsPath \ 6).readNullable[BigDecimal] and
      (JsPath \ 7).readNullable[BigDecimal] and
      (JsPath \ 8).read[Boolean] and
      (JsPath \ 9).readNullable[BigDecimal] and
      (JsPath \ 10).readNullable[BigDecimal] and
      (JsPath \ 11).readNullable[BigDecimal] and
      // skip "sensors"; see StateVector for more information
      (JsPath \ 13).readNullable[BigDecimal] and
      (JsPath \ 14).readNullable[String] and
      (JsPath \ 15).read[Boolean] and
      (JsPath \ 16).read[Int].map(PositionSource.byId) and
      (JsPath \ 17).read[Int].map(AircraftCategory.byId)
    ) (OpenSkyVector.apply _)

  private val multipleStateVectorsReads: Reads[Seq[OpenSkyVector]] = Reads.seq(stateVectorReads)

  sealed trait SingleFileToOpenSkyVectorsResult
  case class SingleFileToOpenSkyVectorsSuccess(vectors: Seq[OpenSkyVector]) extends SingleFileToOpenSkyVectorsResult
  case class SingleFileToOpenSkyVectorsFailure(message: String) extends SingleFileToOpenSkyVectorsResult

  private def doSingleFileToOpenSkyVectors(file: Path): SingleFileToOpenSkyVectorsResult = {

    val fileBytes = Files.readAllBytes(file)

    if (fileBytes.isEmpty) {
      SingleFileToOpenSkyVectorsSuccess(Seq.empty)
    } else {

      val jsValue = Json.parse(fileBytes)

      val jsResultVectors = (jsValue \ "states").validate(multipleStateVectorsReads)
      // If we made stateVectorReads implicit, we could avoid declaring multipleStateVectorsReads
      // and just write ".validate[Seq[StateVector]]". But, the above syntax makes it clearer what's
      // going on.

      jsResultVectors match {

        case JsSuccess(vectors, _) => // TODO Confirm "_" nothing of interest
          SingleFileToOpenSkyVectorsSuccess(vectors)

        case JsError(errors) =>
          SingleFileToOpenSkyVectorsFailure(errors.toString)
      }
    }
  }

  def singleFileToOpenSkyVectors(file: Path): SingleFileToOpenSkyVectorsResult = {
    val successOrFailure = Try(doSingleFileToOpenSkyVectors(file))

    successOrFailure match {
      case Success(result) =>
        result
      case Failure(exception) =>
        SingleFileToOpenSkyVectorsFailure(exception.getMessage)
    }
  }

  case class FailedFileError(file: Path, message: String)
  case class FilesToOpenSkyVectorsResult private (totalFiles: Int, vectors: Seq[OpenSkyVector], errors: Seq[FailedFileError]) {

    def failedFiles: Int = errors.length
    def successFiles: Int = totalFiles - failedFiles

    def updateForSuccessFile(addlVectors: Seq[OpenSkyVector]): FilesToOpenSkyVectorsResult = {
      this.copy(totalFiles = totalFiles + 1, vectors = vectors :++ addlVectors)
    }

    def updateForFailedFile(addlError: FailedFileError): FilesToOpenSkyVectorsResult = {
      this.copy(totalFiles = totalFiles + 1, errors = errors :+ addlError)
    }
  }

  object FilesToOpenSkyVectorsResult {
    def apply(): FilesToOpenSkyVectorsResult = FilesToOpenSkyVectorsResult(0, Seq.empty, Seq.empty)
  }

  def filesToOpenSkyVectors(files: Iterable[Path]): FilesToOpenSkyVectorsResult = {
    files.foldLeft(FilesToOpenSkyVectorsResult()) { case (resultInProgress, file) =>

      singleFileToOpenSkyVectors(file) match {
        case SingleFileToOpenSkyVectorsSuccess(vectors) => resultInProgress.updateForSuccessFile(vectors)
        case SingleFileToOpenSkyVectorsFailure(message) => resultInProgress.updateForFailedFile(FailedFileError(file, message))
      }
    }
  }

  // TODO Could we (easily) use combinator syntax in our Writes instead of what's below?

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
      "airport" -> approachSegmentWithDeviation.threshold.airport.icaoID,
      "threshold" -> approachSegmentWithDeviation.threshold.name,
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

// TODO ** COMMENTED OUT 29 SEPT. 2024 ***
//  val trajectoryWithApproachSegmentsWrites = new Writes[(AircraftProfile, Seq[TimeBasedPositionWithApproachSegment])] {
//
//    override def writes(trajectory: (AircraftProfile, Seq[TimeBasedPositionWithApproachSegment])): JsValue = {
//
//      Json.obj(
//        "icao24" -> trajectory._1.icao24,
//        "callsign" -> trajectory._1.callsign,  // TODO What does this output in "None" case? -- Also, may be relying on default Some.toString, which seems sub-optimal
//        "category" -> trajectory._1.category.map(_.getClass.getSimpleName),  // FIXME Switch to a user-friendly category descriptor
//        "positions" -> multiplePositionWithApproachSegmentWrites.writes(trajectory._2)  // TODO Is this "right"/optimal? Could use combinator syntax instead?
//      )
//    }
//  }
//
//  val trajectoriesWithApproachesWrites = Writes.seq(trajectoryWithApproachSegmentsWrites)

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
