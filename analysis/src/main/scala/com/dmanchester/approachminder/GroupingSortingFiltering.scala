package com.dmanchester.approachminder

import scala.collection.immutable.ListMap

object GroupingSortingFiltering {

  def positionReportsToTrajectories3[R <: HasPositionReportIdentifiers](positionReports: Iterable[R]): Seq[Trajectory3[R]] = {
    val icao24ToPositionReports = partitionElementsByICAO24(positionReports)

    (for {
      (icao24, positionReportsThisICAO24) <- icao24ToPositionReports
      sortedPositionReports = positionReportsThisICAO24.sortBy(_.timePosition)
      cleanedPositionReports = resolveTimeConflicts(sortedPositionReports)
      categories = cleanedPositionReports.map(_.category)
      mostCommonCategory = AircraftCategory.mostCommonNonBlankCategoryInNonEmptyCollection(categories)
      partitionedReports = ReportsPartitioner.partition(cleanedPositionReports, 666)  // FIXME Take as argument
      (callsign, reports) <- partitionedReports
    } yield {
      Trajectory3.createOption(reports, icao24, callsign, mostCommonCategory)
    }).flatten

    // FIXME Either here or in calling code, filter down to possibly FixedWingPowered aircraft/trajectories; the code
    //  and comments from old filterPossiblyFixedWingPowered method:
    //
    // Retain only those aircraft whose category suggests a fixed-wing, powered aircraft, as well
    // as those with no category at all. (We achieve the latter via `forall`: given an `Option`,
    // it returns `true` on `None`).
    //
    // category.forall(AircraftCategory.fixedWingPowered.contains(_))


    // Version before for-comprehension (TODO delete):
    //
    //    icao24ToPositionReports.flatMap { case (icao24, positionReportsThisICAO24) =>
    //
    //      val sortedPositionReports = positionReportsThisICAO24.sortBy(_.timePosition)
    //      val cleanedPositionReports = resolveTimeConflicts(sortedPositionReports)
    //
    //      val categories = cleanedPositionReports.map(_.category)
    //      val mostCommonCategory = AircraftCategory.mostCommonNonBlankCategoryInNonEmptyCollection(categories)
    //
    //      val partitionedReports = ReportsPartitioner.partition(cleanedPositionReports, 666)  // FIXME Take as argument
    //
    //      partitionedReports.map { case (callsign, reports) =>
    //        val positions = reports.map(positionReportToTrajectoryPosition)
    //        Trajectory3(icao24, callsign, mostCommonCategory, positions)
    //      }
    //    }
    //}
  }

  def partitionElementsByICAO24[I <: HasICAO24](elements: Iterable[I]): Seq[(String, Seq[I])] = {

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
}
