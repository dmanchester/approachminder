package com.dmanchester.approachminder

import scala.annotation.tailrec

object GroupingSortingFiltering {

  def fullySpecifiedPositions(uniqueVectors: Seq[StateVector]): Seq[TimeBasedPosition] = {
    uniqueVectors.flatMap(TimeBasedPosition.option(_)) // flatMap's mapping operation turns the `Seq[StateVector]` into a `Seq[Option[TimeBasedPosition]]`. Its flattening operation eliminates the `None` elements and produces a `Seq[TimeBasedPosition]`.
  }

  def positionsByAircraft(positions: Seq[TimeBasedPosition]): Map[AircraftProfile, Seq[TimeBasedPosition]] = {

    positions.groupBy(_.vector.icao24).map { case (icao24, positionsOneAircraftUnsorted) =>

      val callsigns = positionsOneAircraftUnsorted.flatMap(_.vector.callsign)  // flatMap, because StateVector.callsign is an Option[String]
      val theMostCommonCallsign = mostCommonString(callsigns)

      val categories = positionsOneAircraftUnsorted.map(_.vector.category)
      val theMostCommonNonBlankCategory = mostCommonNonBlankCategory(categories)

      val profile = AircraftProfile(icao24, theMostCommonCallsign, theMostCommonNonBlankCategory)

      (profile, positionsOneAircraftUnsorted.sortBy(_.timePosition))
    }
  }

  /**
   * Determine the most-common string in an `Iterable`.
   *
   * If multiple strings are equally common, pick the one that is alphabetically first (according to
   * the inherent ordering of `String`).
   *
   * @param strings
   * @return
   */
  def mostCommonString(strings: Iterable[String]): Option[String] = {
    mostCommonValue(strings) { (a, b) => a < b }
  }

  /**
   * Determine the most-common aircraft category that is "non-blank" (i.e., not `NoInfoAtAll` or
   * `NoADSBEmitterCategoryInfo`).
   *
   * If multiple categories are equally common, pick the one that is alphabetically first by class
   * name. (This is just to ensure deterministic behavior regardless of the categories' ordering.)
   *
   * @param categories
   * @return the most-common `AircraftCategory` as a `Some`; or, `None` if all categories are
   *         `NoInfoAtAll`/`NoADSBEmitterCategoryInfo` or if `categories` is empty.
   */
  def mostCommonNonBlankCategory(categories: Iterable[AircraftCategory]): Option[AircraftCategory] = {
    val nonBlankCategories = categories.filter(!AircraftCategory.blank.contains(_))
    mostCommonValue(nonBlankCategories) { (a, b) => a.getClass.getSimpleName < b.getClass.getSimpleName }
  }

  /**
   * Determine the most-common value in an `Iterable`.
   *
   * If multiple values are equally common (i.e., they have the same number of occurrences), pick
   * the one that comes first according to a user-supplied discriminator function.
   *
   * @param values
   * @param comesFirst Discriminator function. Given `a` and `b`, returns which one comes first.
   * @tparam T
   * @return
   */
  private def mostCommonValue[T](values: Iterable[T])(comesFirst: (T, T) => Boolean): Option[T] = {

    val valuesAndCounts = values.groupBy(identity).map { case (value, valueOccurrences) =>
      (value, valueOccurrences.size)
    }.toSeq

    // Pick value and count "a" if:
    //
    //   * a's count is higher than b's; or,
    //   * their counts are the same, but "a" comes first, according to the discriminator function.
    //
    // Otherwise, pick "b".
    val mostCommonValueWithCount = valuesAndCounts.reduceOption { (a, b) =>
      if (a._2 > b._2 || (a._2 == b._2 && comesFirst(a._1, b._1)))
        a
      else
        b
    }

    mostCommonValueWithCount.map(_._1)
  }

  def filterPossiblyFixedWingPowered(positionsUnfiltered: Map[AircraftProfile, Seq[TimeBasedPosition]]): Map[AircraftProfile, Seq[TimeBasedPosition]] = {
    positionsUnfiltered.filter { case (aircraftProfile, _) =>
      // Retain only those aircraft whose category suggests a fixed-wing, powered aircraft, as well
      // as those with no category at all. (We achieve the latter via `forall`: given an `Option`,
      // it returns `true` on `None`).
      aircraftProfile.category.forall(AircraftCategory.fixedWingPowered.contains(_))
    }
  }

  def trajectories[T <: HasTime](positionsByAircraft: Map[AircraftProfile, Seq[T]], minTimeInSecondsForSegmenting: Int): Seq[(AircraftProfile, Trajectory[T])] = {

    positionsByAircraft.toSeq.flatMap { case (aircraftProfile, positionsOneAircraft) =>

      val cleanedPositions = cleanPositionsWithSameTime(positionsOneAircraft)

      val trajectories = segmentIntoTrajectoriesByTime(cleanedPositions, minTimeInSecondsForSegmenting)

      // Wrap each trajectory in a tuple, with its aircraft profile as the first element.
      trajectories.map((aircraftProfile, _))
    }
  }

  /**
   * Clean positions with the same time, picking the position furthest down in `positions` as the
   * winner and discarding the others of that time.
   *
   * @param positions The positions. Must be in ascending time order!
   * @return
   */
  def cleanPositionsWithSameTime[T <: HasTime](positions: Seq[T]): Seq[T] = {

    if (positions.isEmpty) {
      Seq.empty[T]
    } else {

      // Given a sequence of positions from 0 to n, start with n as the first cleaned position. Step
      // in reverse from n-1 to 0, adding a position to the cleaned positions unless its time
      // matches that of the previously added position.

      val cleanedPositionsInitial = Seq(positions.last)

      positions.dropRight(1).foldRight(cleanedPositionsInitial) { case (position, cleanedPositions) =>
        if (position.timePosition == cleanedPositions.head.timePosition) {
          cleanedPositions // don't add position
        } else {
          position +: cleanedPositions
        }
      }
    }
  }

  /**
   * Segment an aircraft's historical positions into a series of trajectories, ending one trajectory
   * and beginning the next if adjoining positions are separated by at least a specified amount of
   * time.
   *
   * FIXME This method will not work "right" if the positions are not time-ordered. Enforce that invariant via a PositionsHistory type?
   *
   * @param historicalPositions
   * @param minTimeInSeconds
   * @return the trajectories; or, an empty `Seq`, if `historicalPositions` is empty or if no trajectories could be
   *         created (the uncommon case that all positions were separated by at least `minTimeInSeconds`).
   */
  def segmentIntoTrajectoriesByTime[T <: HasTime](historicalPositions: Seq[T], minTimeInSeconds: Int): Seq[Trajectory[T]] = {

    if (historicalPositions.isEmpty) {
      Seq.empty[Trajectory[T]]
    } else {

      // Begin a first trajectory with the first position.
      val trajectoryInProgress = Seq(historicalPositions.head)
      val remainingHistoricalPositions = historicalPositions.tail

      doSegmentIntoTrajectoriesByTime(remainingHistoricalPositions.iterator, minTimeInSeconds, trajectoryInProgress, Seq.empty[Trajectory[T]])
    }
  }

  @tailrec private def doSegmentIntoTrajectoriesByTime[T <: HasTime](historicalPositionsIterator: Iterator[T], minTimeInSeconds: Int, trajectoryInProgress: Seq[T], completedTrajectories: Seq[Trajectory[T]]): Seq[Trajectory[T]] = {

    if (!historicalPositionsIterator.hasNext) {

      // Complete the final trajectory and return them all.
      completedTrajectories :++ Trajectory.newOption(trajectoryInProgress)

    } else {

      val currentPosition = historicalPositionsIterator.next()
      val previousPosition = trajectoryInProgress.last
      val positionsSeparatedByAtLeastMinTime = currentPosition.timePosition - previousPosition.timePosition >= minTimeInSeconds

      val (completedTrajectoriesUpdated, trajectoryInProgressUpdated) = if (positionsSeparatedByAtLeastMinTime) {
        // Complete the in-progress trajectory and begin a new one.
        (completedTrajectories :++ Trajectory.newOption(trajectoryInProgress), Seq(currentPosition))
      } else {
        // Append the current position to the in-progress trajectory.
        (completedTrajectories, trajectoryInProgress :+ currentPosition)
      }

      doSegmentIntoTrajectoriesByTime(historicalPositionsIterator, minTimeInSeconds, trajectoryInProgressUpdated, completedTrajectoriesUpdated)
    }
  }
}
