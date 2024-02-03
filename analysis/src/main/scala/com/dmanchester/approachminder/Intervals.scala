package com.dmanchester.approachminder

import scala.collection.Searching.{Found, InsertionPoint}

class Intervals[T <: Ordered[T]] private(points: IndexedSeq[T]) {

  val min = points.head
  val max = points.last

  // TODO Add tests. Add length check and tests (require 1, or 2? Maybe 2, as 1 case may be degenerate?)
  // TODO Document short-circuit that precedes points.search below

  def search(value: T): IntervalsSearchResult[T] = {

    if (value > max) {
      GreaterThanMax
    } else if (value < min) {
      LessThanMin
    } else {
      points.search(value) match {
        case InsertionPoint(x) => new BetweenPoints(points(x - 1), points(x))
        case Found(_)          =>     MatchesAPoint
      }
    }
  }
}

sealed trait IntervalsSearchResult[+T]

case class BetweenPoints[T](val a: T, val b: T) extends IntervalsSearchResult[T]
case object MatchesAPoint extends IntervalsSearchResult[Nothing]
case object LessThanMin extends IntervalsSearchResult[Nothing]
case object GreaterThanMax extends IntervalsSearchResult[Nothing]

object Intervals {

  def fromPointsSet[T <: Ordered[T]](points: Set[T]): Intervals[T] = {
    // TODO Enforce non-empty Set, return Option[Intervals[T]]?
    val pointsOrdered = points.toIndexedSeq.sorted
    new Intervals(pointsOrdered)
  }
}
