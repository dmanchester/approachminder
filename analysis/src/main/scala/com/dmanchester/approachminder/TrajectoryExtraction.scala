package com.dmanchester.approachminder

import java.nio.file.Path
import scala.collection.immutable.ListMap

object TrajectoryExtraction {

  def partitionByICAO24[I <: HasICAO24](elements: Iterable[I]): Seq[(String, Seq[I])] = {

    val icao24ToElements: Map[String, Seq[I]] = elements.foldLeft(ListMap.empty[String, Seq[I]]) { case (map, element) =>
      val icao24 = element.icao24
      val seqToUpdate = map.getOrElse(icao24, Seq.empty[I])
      map.updated(icao24, seqToUpdate :+ element)
    }

    icao24ToElements.toSeq
  }

  /**
   * Resolves time conflicts among elements. More specifically, given multiple elements having the same time, picks the
   * element furthest down in `timeSortedElements` as the winner and discards the other elements with that time.
   *
   * @param timeSortedElements Must be sorted (ascending).
   * @tparam T
   * @return
   */
  def resolveTimeConflicts[T <: HasTime](timeSortedElements: Seq[T]): Seq[T] = {

    if (timeSortedElements.isEmpty) {
      Seq.empty[T]
    } else {

      // Given a sequence of elements from 0 to n, start with n as the first cleaned position. Step in reverse from n-1
      // to 0, adding an element to the cleaned elements unless its time matches that of the previously added element.

      val cleanedSeqInitial = Seq(timeSortedElements.last)

      timeSortedElements.init.foldRight(cleanedSeqInitial) { case (element, cleanedSeqInProgress) =>
        val lastAddedTimePosition = cleanedSeqInProgress.head.timePosition
        if (element.timePosition == lastAddedTimePosition) {
          cleanedSeqInProgress // don't add element
        } else {
          element +: cleanedSeqInProgress
        }
      }
    }
  }

  def positionReportsToTrajectories[R <: HasPositionReportIdentifiers](positionReports: Iterable[R], timeGapForPartitioning: Int): Seq[Trajectory3[R]] = {
    val icao24ToPositionReports = partitionByICAO24(positionReports)

    (for {
      (icao24, positionReportsThisICAO24) <- icao24ToPositionReports
      sortedPositionReports = positionReportsThisICAO24.sortBy(_.timePosition)
      cleanedPositionReports = resolveTimeConflicts(sortedPositionReports)
      categories = cleanedPositionReports.map(_.category)
      mostCommonCategory = AircraftCategory.mostCommonNonBlankCategoryInNonEmptyCollection(categories)
      partitionedReports = ReportsPartitioner.partitionOnTimeGapAndCallsignChange(cleanedPositionReports, timeGapForPartitioning)
      (callsign, reports) <- partitionedReports
    } yield {
      Trajectory3.createOption(reports, icao24, callsign, mostCommonCategory)
    }).flatten
  }

  /**
   * Trajectories restricted to those that are possibly fixed-wing powered.
   *
   * @param fileGlob
   * @return
   */
  def openSkyFilesToTrajectories(dir: Path, glob: String, timeGapForPartitioning: Int): Seq[Trajectory3[OpenSkyPositionReport]] = {

    // TODO Change dir to a String and do Paths.get inside this method?
    // TODO Make println's into log statements

    val files = IO.resolveGlob(dir, glob)
    println(s"${files.length} files to be read...")

    val filesResult = IO.openSkyFilesToVectors(files)
    println(s"${filesResult.totalFiles} files read (success: ${filesResult.successFiles}; failure: ${filesResult.failedFiles})")

    val positionReportsAllFields = filesResult.vectors.flatMap(_.toPositionReportAllFields)
    println(s"${filesResult.vectors.length} vectors distilled to ${positionReportsAllFields.length} position reports")
    val trajectoriesUnfiltered = positionReportsToTrajectories(positionReportsAllFields, timeGapForPartitioning)
    val trajectories = trajectoriesUnfiltered.filter(_.isPossiblyFixedWingPowered)
    println(s"${trajectories.length} trajectories (${trajectoriesUnfiltered.length} before filtering)")
    trajectories.map(_.mapPositions(_.toPositionReport))
  }
}
