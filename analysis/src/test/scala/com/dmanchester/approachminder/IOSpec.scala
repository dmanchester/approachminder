package com.dmanchester.approachminder

import com.dmanchester.approachminder.IO.{SingleFileToOpenSkyVectorsFailure, SingleFileToOpenSkyVectorsSuccess}
import org.specs2.mutable._

import java.nio.file.{Path, Paths}

class IOSpec extends Specification {

  private val vectorLAU802 = OpenSkyVector(
    "abcd12",
    Some("LAU802"),
    "United States",
    Some(1668315955),
    1668315955,
    Some(-122.2671),
    Some(37.3897),
    Some(1767.84),
    false,
    Some(131.95),
    Some(93.58),
    Some(-0.33),
    Some(1783.08),
    Some("3757"),
    false,
    PositionSource.byId(0),
    AircraftCategory.byId(1)
  )

  private val vectorLAU1212 = OpenSkyVector(
    "cdef34",
    Some("LAU1212"),
    "Germany",
    Some(1668315955),
    1668315955,
    Some(-122.729),
    Some(38.0925),
    Some(5105.4),
    false,
    Some(217.63),
    Some(166.88),
    Some(-10.4),
    Some(5120.64),
    None,
    false,
    PositionSource.byId(0),
    AircraftCategory.byId(0)
  )

  private val pathEmptyJson = getResourcePath("empty.json")
  private val pathTwoVectors = getResourcePath("two-vectors.json")
  private val pathNotJson = getResourcePath("not-json.txt")
  private val pathOneVectorTwoNonJsonLines = getResourcePath("one-vector--two-non-json-lines.json")
  private val pathOneGoodVectorOneBad = getResourcePath("one-good-vector--one-bad-vector.json")

  "singleFileToOpenSkyVectors" should {

    "handle an empty input file" in {
      val result = IO.singleFileToOpenSkyVectors(pathEmptyJson)
      result mustEqual SingleFileToOpenSkyVectorsSuccess(Seq.empty)
    }

    "process a typical file correctly" in {
      val result = IO.singleFileToOpenSkyVectors(pathTwoVectors)
      result mustEqual SingleFileToOpenSkyVectorsSuccess(Seq(
        vectorLAU802,
        vectorLAU1212
      ))
    }

    "handle a file that isn't JSON" in {
      val result = IO.singleFileToOpenSkyVectors(pathNotJson)
      result must beLike {
        case SingleFileToOpenSkyVectorsFailure(message) =>
          message must startWith("Unrecognized token 'This'")
      }
    }

    "handle a file with mixed JSON/non-JSON content (OK to reject whole file)" in {
      val result = IO.singleFileToOpenSkyVectors(pathOneVectorTwoNonJsonLines)
      result must beLike {
        case SingleFileToOpenSkyVectorsFailure(message) =>
          message must startWith("Unrecognized token 'That'")
      }
    }

    "handle a file with bad vectors (OK to reject whole file)" in {
      val result = IO.singleFileToOpenSkyVectors(pathOneGoodVectorOneBad)
      result must beLike {
        case SingleFileToOpenSkyVectorsFailure(message) =>
          message must contain("error.expected.jsstring")
      }
    }
  }

  "filesToOpenSkyVectors" should {
    "handle a mix of good and bad files" in {

      val paths = Seq(
        pathEmptyJson,  // will succeed
        pathTwoVectors,  // will succeed
        pathNotJson,  // will fail
        pathOneVectorTwoNonJsonLines,  // will fail
        pathTwoVectors,  // will succeed
        pathEmptyJson,  // will succeed
        pathOneGoodVectorOneBad  // will fail
      )
      val result = IO.filesToOpenSkyVectors(paths)

      result.totalFiles mustEqual 7
      result.successFiles mustEqual 4
      result.failedFiles mustEqual 3

      result.vectors mustEqual Seq(
        vectorLAU802,
        vectorLAU1212,
        vectorLAU802,
        vectorLAU1212
      )

      result.errors.length mustEqual 3
      // Spot-check the middle error
      result.errors(1).file mustEqual pathOneVectorTwoNonJsonLines
      result.errors(1).message must startWith("Unrecognized token 'That'")
    }
  }

  private def getResourcePath(filename: String): Path = Paths.get(getClass.getResource(s"resources/$filename").toURI)
}