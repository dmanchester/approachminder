package com.dmanchester.approachminder

import play.api.libs.functional.syntax.{toFunctionalBuilderOps, unlift}
import play.api.libs.json.Reads._
import play.api.libs.json._

import java.nio.file.{Files, Path}
import java.time.Instant
import java.time.format.DateTimeFormatter
import scala.collection.mutable
import scala.jdk.CollectionConverters._
import scala.util.Using

object IO {

  val stateVectorReads: Reads[StateVector] = (
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
    ) (StateVector.apply _)

  val multipleStateVectorsReads: Reads[Seq[StateVector]] = Reads.seq(stateVectorReads)

  // TODO Could we (easily) use combinator syntax in our Writes instead of what's below?

  val multipleTimeBasedPositionWrites = new Writes[Seq[TimeBasedPosition]] {

    override def writes(timeBasedPositions: Seq[TimeBasedPosition]): JsValue = {

      JsObject( timeBasedPositions.map { timeBasedPosition =>

        val instant = Instant.ofEpochSecond(timeBasedPosition.timePosition.toLong)
        val formattedInstant = DateTimeFormatter.ISO_INSTANT.format(instant)

        formattedInstant -> Json.obj(
          "longitude" -> timeBasedPosition.longitude,
          "latitude" -> timeBasedPosition.latitude,
          "altitude" -> timeBasedPosition.altitudeMeters
        )
      })
    }
  }

  val trajectoryWrites = new Writes[(AircraftProfile, Seq[TimeBasedPosition])] {

    override def writes(trajectory: (AircraftProfile, Seq[TimeBasedPosition])): JsValue = {

      Json.obj(
        "icao24" -> trajectory._1.icao24,
        "callsign" -> trajectory._1.callsign,  // TODO What does this output in "None" case? -- Also, may be relying on default Some.toString, which seems sub-optimal
        "category" -> trajectory._1.category.map(_.getClass.getSimpleName),  // FIXME Switch to a user-friendly category descriptor
        "positions" -> multipleTimeBasedPositionWrites.writes(trajectory._2)  // TODO Is this "right"/optimal? Could use combinator syntax instead?
      )
    }
  }

  val trajectoriesWrites = Writes.seq(trajectoryWrites)

  def resolveGlob(dir: Path, glob: String): Seq[Path] = {

    Using.resource(Files.newDirectoryStream(dir, glob)) { dirStreamAsJavaIterable: java.lang.Iterable[Path] =>
      dirStreamAsJavaIterable.asScala.toSeq
    }
  }

  class ReadUniqueVectorsResult(val filesReadSuccessfully: Int, val totalVectorsRead: Int, val uniqueVectors: Seq[StateVector], val filesErroredOut: Int, val errors: Seq[JsError]) {

    override def toString = s"${this.getClass.getSimpleName}(filesReadSuccessfully:$filesReadSuccessfully,totalVectorsRead:$totalVectorsRead,uniqueVectors.length:${uniqueVectors.length},filesErroredOut:$filesErroredOut,errors.length:${errors.length})"  // styled after case classes' toString
  }

  def readUniqueVectors(files: Seq[Path]): ReadUniqueVectorsResult = {

    // TODO Explain why using mutable
    var filesReadSuccessfully = 0
    var totalVectorsRead = 0
    val uniqueVectors: mutable.Set[StateVector] = mutable.LinkedHashSet.empty
    var filesErroredOut = 0
    val errors: mutable.Buffer[JsError] = mutable.Buffer.empty  // mutable.Seq doesn't offer in-place "add..." methods, but mutable.Buffer does

    files.foreach({ file =>

      val fileBytes = Files.readAllBytes(file)
      val json = Json.parse(fileBytes)

      val jsResultStateVectors = (json \ "states").validate(multipleStateVectorsReads)
      // If we made stateVectorReads implicit, we could avoid declaring multipleStateVectorsReads
      // and just write ".validate[Seq[StateVector]]". But, the above syntax makes it clearer what's
      // going on.

      jsResultStateVectors match {

        case JsSuccess(vectorsFromFile, _) => {  // TODO Confirm "_" nothing of interest
          filesReadSuccessfully += 1
          totalVectorsRead += vectorsFromFile.length
          uniqueVectors.addAll(vectorsFromFile)
        }

        case error: JsError => {
          filesErroredOut += 1
          errors.addOne(error)
        }
      }
    })

    // TODO Confirm what I get from toSeq is immutable
    new ReadUniqueVectorsResult(filesReadSuccessfully, totalVectorsRead, uniqueVectors.toSeq, filesErroredOut, errors.toSeq)
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
