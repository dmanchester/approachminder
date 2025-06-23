package com.dmanchester.approachminder.utils

import com.dmanchester.approachminder.{AircraftCategory, OpenSkyVector, PositionSource}
import play.api.libs.functional.syntax.toFunctionalBuilderOps
import play.api.libs.json.*
import play.api.libs.json.Reads.*

import java.nio.file.{Files, Path}
import scala.jdk.CollectionConverters.*
import scala.util.{Failure, Success, Try, Using}

object Input {

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

  sealed trait SingleOpenSkyFileToVectorsResult
  case class SingleOpenSkyFileToVectorsSuccess(vectors: Seq[OpenSkyVector]) extends SingleOpenSkyFileToVectorsResult
  case class SingleOpenSkyFileToVectorsFailure(message: String) extends SingleOpenSkyFileToVectorsResult

  private def doSingleOpenSkyFileToVectors(file: Path): SingleOpenSkyFileToVectorsResult = {

    val fileBytes = Files.readAllBytes(file)

    if (fileBytes.isEmpty) {
      SingleOpenSkyFileToVectorsSuccess(Seq.empty)
    } else {

      val jsValue = Json.parse(fileBytes)

      val jsResultVectors = (jsValue \ "states").validate(multipleStateVectorsReads)
      // If we made stateVectorReads implicit, we could avoid declaring multipleStateVectorsReads
      // and just write ".validate[Seq[StateVector]]". But, the above syntax makes it clearer what's
      // going on.

      jsResultVectors match {

        case JsSuccess(vectors, _) => // TODO Confirm "_" nothing of interest
          SingleOpenSkyFileToVectorsSuccess(vectors)

        case JsError(errors) =>
          SingleOpenSkyFileToVectorsFailure(errors.toString)
      }
    }
  }

  def singleOpenSkyFileToVectors(file: Path): SingleOpenSkyFileToVectorsResult = {
    val successOrFailure = Try(doSingleOpenSkyFileToVectors(file))

    successOrFailure match {
      case Success(result) =>  // "success" in this case doesn't necessarily mean method doSingleOpenSkyFileToVectors succeeded (result can be a OpenSkyFileToVectorsFailure); only that the method didn't throw
        result
      case Failure(exception) =>
        SingleOpenSkyFileToVectorsFailure(exception.getMessage)
    }
  }

  case class FailedFileError(file: Path, message: String)
  case class OpenSkyFilesToVectorsResult private(totalFiles: Int, vectors: Seq[OpenSkyVector], errors: Seq[FailedFileError]) {

    def failedFiles: Int = errors.length
    def successFiles: Int = totalFiles - failedFiles

    def updateForSuccessFile(addlVectors: Seq[OpenSkyVector]): OpenSkyFilesToVectorsResult = {
      this.copy(totalFiles = totalFiles + 1, vectors = vectors :++ addlVectors)
    }

    def updateForFailedFile(addlError: FailedFileError): OpenSkyFilesToVectorsResult = {
      this.copy(totalFiles = totalFiles + 1, errors = errors :+ addlError)
    }
  }

  object OpenSkyFilesToVectorsResult {
    val empty = OpenSkyFilesToVectorsResult(0, Seq.empty, Seq.empty)
  }

  def openSkyFilesToVectors(files: Iterable[Path]): OpenSkyFilesToVectorsResult = {
    files.foldLeft(OpenSkyFilesToVectorsResult.empty) { case (resultInProgress, file) =>

      singleOpenSkyFileToVectors(file) match {
        case SingleOpenSkyFileToVectorsSuccess(vectors) => resultInProgress.updateForSuccessFile(vectors)
        case SingleOpenSkyFileToVectorsFailure(message) => resultInProgress.updateForFailedFile(FailedFileError(file, message))
      }
    }
  }
}
