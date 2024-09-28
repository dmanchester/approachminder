package com.dmanchester.approachminder

import scala.collection.immutable.ListMap

object GroupingSortingFiltering {

  def positionReportsToTrajectories3[R <: HasPositionReportIdentifiers, P](positionReports: Iterable[R], positionReportToTrajectoryPosition: R => P): Seq[Trajectory3[P]] = {
    val icao24ToPositionReports = partitionElementsByICAO24(positionReports)
    val jonxy = icao24ToPositionReports.map { case (icao24, positionReportsThisICAO24) =>
      val sortedPositionReports = positionReportsThisICAO24.sortBy(_.timePosition)
      val cleanedPositionReports = resolveTimeConflicts(sortedPositionReports)

      // DAN YOU LEFT OFF HERE. GET THE MOST-COMMON CATEGORY.
      // THEN, PARTITION ON TIME GAP OR CALLSIGN CHANGE (three-state state machine).
      // ASSEMBLE ICAO24, CATEGORY, AND EACH PARTITION'S CALLSIGN AND POSITIONREPORTS INTO A TRAJECOTRY3.


    }
null
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


  def fullySpecifiedPositions(uniqueVectors: Seq[OpenSkyVector]): Seq[TimeBasedPosition] = {
    uniqueVectors.flatMap(TimeBasedPosition.option(_)) // flatMap's mapping operation turns the `Seq[StateVector]` into a `Seq[Option[TimeBasedPosition]]`. Its flattening operation eliminates the `None` elements and produces a `Seq[TimeBasedPosition]`.
  }

  def positionsByAircraft(positions: Seq[TimeBasedPosition]): Map[AircraftProfile, Seq[TimeBasedPosition]] = {

    positions.groupBy(_.vector.icao24).map { case (icao24, positionsOneAircraftUnsorted) =>

      val callsigns = positionsOneAircraftUnsorted.flatMap(_.vector.callsign)  // flatMap, because StateVector.callsign is an Option[String]
      val theMostCommonCallsign = Utils.mostCommonString(callsigns)

      val categories = positionsOneAircraftUnsorted.map(_.vector.category)
      val theMostCommonNonBlankCategory = AircraftCategory.mostCommonNonBlankCategoryInNonEmptyCollection(categories)

      val profile = AircraftProfile(icao24, theMostCommonCallsign, theMostCommonNonBlankCategory)

      (profile, positionsOneAircraftUnsorted.sortBy(_.timePosition))
    }
  }

  def filterPossiblyFixedWingPowered(positionsUnfiltered: Map[AircraftProfile, Seq[TimeBasedPosition]]): Map[AircraftProfile, Seq[TimeBasedPosition]] = {
    positionsUnfiltered.filter { case (aircraftProfile, _) =>
      // Retain only those aircraft whose category suggests a fixed-wing, powered aircraft, as well
      // as those with no category at all. (We achieve the latter via `forall`: given an `Option`,
      // it returns `true` on `None`).
      aircraftProfile.category.forall(AircraftCategory.fixedWingPowered.contains(_))
    }
  }
}
