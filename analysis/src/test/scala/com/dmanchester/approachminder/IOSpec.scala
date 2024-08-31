package com.dmanchester.approachminder

import com.dmanchester.approachminder.IO.{FileToOpenSkyVectorsFailure, FileToOpenSkyVectorsSuccess}
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

  "fileToOpenSkyVectors" should {

    "handle an empty input file" in {
      val path = getResourcePath("empty.json")
      val result = IO.fileToOpenSkyVectors(path)
      result mustEqual FileToOpenSkyVectorsSuccess(Seq.empty)
    }

    "process a typical file correctly" in {
      val path = getResourcePath("two-vectors.json")
      val result = IO.fileToOpenSkyVectors(path)
      result mustEqual FileToOpenSkyVectorsSuccess(Seq(
        vectorLAU802,
        vectorLAU1212
      ))
    }

    "handle a file that isn't JSON" in {
      val path = getResourcePath("not-json.txt")
      val result = IO.fileToOpenSkyVectors(path)
      result must beLike {
        case FileToOpenSkyVectorsFailure(message) =>
          message must startWith("Unrecognized token 'This'")
      }
    }

    "handle a file with mixed JSON/non-JSON content (OK to reject whole file)" in {
      val path = getResourcePath("one-vector--two-non-json-lines.json")
      val result = IO.fileToOpenSkyVectors(path)
      result must beLike {
        case FileToOpenSkyVectorsFailure(message) =>
          message must startWith("Unrecognized token 'That'")
      }
    }

    "handle a file with bad vectors (OK to reject whole file)" in {
      val path = getResourcePath("one-good-vector--one-bad-vector.json")
      val result = IO.fileToOpenSkyVectors(path)
      result must beLike {
        case FileToOpenSkyVectorsFailure(message) =>
          message must contain("error.expected.jsstring")
      }
    }
  }

  private def getResourcePath(filename: String): Path = Paths.get(getClass.getResource(s"resources/$filename").toURI)
}