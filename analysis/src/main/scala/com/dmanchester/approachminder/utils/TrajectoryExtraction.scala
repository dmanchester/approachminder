package com.dmanchester.approachminder.utils

import com.dmanchester.approachminder.typeswithbehavior.Trajectory
import com.dmanchester.approachminder.typeswithoutbehavior.*
import com.typesafe.scalalogging.StrictLogging

import scala.collection.immutable.ListMap

object TrajectoryExtraction extends StrictLogging {

  /**
   * Partition elements (typically, position reports) by ICAO24 identifier (so, by aircraft).
   *
   * @param elements The elements to partition.
   * @tparam I The elements' type.
   * @return The partitioned elements, keyed by ICAO24. The order of the input is maintained.
   */
  def partitionByICAO24[I <: HasICAO24](elements: Iterable[I]): Seq[(String, Seq[I])] = {

    // Conceptually, this is a group-by operation, with the added guarantee of elements' order being maintained.
    val icao24ToElements: Map[String, Seq[I]] = elements.foldLeft(ListMap.empty[String, Seq[I]]) { case (map, element) =>
      val icao24 = element.icao24
      val seqToUpdate = map.getOrElse(icao24, Seq.empty[I])
      map.updated(icao24, seqToUpdate :+ element)
    }

    icao24ToElements.toSeq
  }

  /**
   * Resolve time conflicts among elements. More specifically, given multiple elements having the same time, pick the
   * element furthest down in `timeSortedElements` as the winner and discard the other elements with that time.
   *
   * TODO Add tests.
   *
   * @param timeSortedElements Must be sorted (ascending)!
   * @tparam T The elements' type.
   * @return The elements, with time conflicts resolved.
   */
  private def resolveTimeConflicts[T <: HasTime](timeSortedElements: Seq[T]): Seq[T] = {

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

  /**
   * Create trajectories from a series of position reports.
   *
   * TODO Add tests.
   *
   * @param reports The reports.
   * @param timeGapSecsForPartitioning The time gap on which to partition the reports for a given callsign. A gap of at
   *                                   least this many seconds will lead to a new trajectory for that callsign.
   * @tparam R The reports' type.
   * @return The trajectories.
   */
  private def positionReportsToTrajectories[R <: HasPositionReportIdentifiers](reports: Iterable[R], timeGapSecsForPartitioning: Int): Seq[Trajectory[R]] = {

    val icao24ToReports = partitionByICAO24(reports)

    (for {
      (icao24, reportsThisICAO24) <- icao24ToReports
      sortedReports = reportsThisICAO24.sortBy(_.timePosition)
      cleanedReports = resolveTimeConflicts(sortedReports)
      categories = cleanedReports.map(_.category)
      mostCommonCategory = CollectionUtils.mostCommonNonBlankCategoryInNonEmptyCollection(categories)
      partitionedReports = ReportsPartitioning.partition(cleanedReports, timeGapSecsForPartitioning)
      (callsign, reports) <- partitionedReports
    } yield {
      Trajectory.newOption(reports, icao24, callsign, mostCommonCategory)
    }).flatten
  }

  /**
   * Parse a series of JSON files from the OpenSky API and produce trajectories from them.
   *
   * TODO Add tests.
   *
   * @param dir The directory containing the files.
   * @param glob A glob that identifies the files (typically via wildcard).
   * @param timeGapSecsForPartitioning The time gap on which to partition the trajectories for a given callsign. A gap
   *                                   of at least this many seconds will lead to a new trajectory for that callsign.
   * @return The trajectories. -- They package instances of OpenSkyPositionReport, as opposed to
   *         OpenSkyPositionReportAllFields, to lead client code to look up fairly static values (like aircraft
   *         category) in the Trajectory object itself (where they have been rolled up via established procedures), as
   *         opposed to looking them up in arbitrary position reports.
   */
  def openSkyFilesToTrajectories(dir: String, glob: String, timeGapSecsForPartitioning: Int): Seq[Trajectory[OpenSkyPositionReport]] = {

    val files = Input.resolveGlob(dir, glob)
    logger.info(s"${files.length} files to be read...")

    val filesResult = Input.openSkyFilesToVectors(files)
    logger.info(s"${filesResult.totalFiles} files read (success: ${filesResult.successFiles}; failure: ${filesResult.failedFiles})")

    val positionReportsAllFields = filesResult.vectors.flatMap(OpenSkyPositionReportAllFields.fromVector)
    logger.info(s"${filesResult.vectors.length} vectors distilled to ${positionReportsAllFields.length} position reports")
    val trajectoriesWithPositionReportsAllFields = positionReportsToTrajectories(positionReportsAllFields, timeGapSecsForPartitioning)
    val trajectories = trajectoriesWithPositionReportsAllFields.map(_.mapPositions(OpenSkyPositionReport.fromPositionReportAllFields))

    logger.info(s"${trajectories.length} trajectories created")
    trajectories
  }
}
