package com.dmanchester.approachminder

import scala.collection.BuildFrom
import scala.collection.immutable.ListMap

/**
 * Contains a `Seq` of `HasICAO24` elements specific to an aircraft. That specificity follows from ensuring all elements
 * have the same `icao24` value.
 */
case class AircraftSpecificData[I <: HasICAO24, S[X] <: Seq[X]] private (seq: S[I])

object AircraftSpecificData {

  /**
   * Groups a multi-aircraft `Seq` of `HasICAO24` elements by `icao24` value, producing an `AircraftSpecificData` for
   * each one.
   *
   * Each `AircraftSpecificData` instance's elements retain their ordering from the source `Seq`.
   *
   * Additionally, the `AircraftSpecificData` instances themselves are ordered by when an `icao24` value first appeared
   * in the source `Seq`.
   *
   *
   * @param sourceSeq
   * @param bf
   * @tparam I
   * @tparam S
   * @return
   */
  def createMultiple[I <: HasICAO24, S[X] <: Seq[X]](sourceSeq: S[I])(implicit bf: BuildFrom[S[I], I, S[I]]): Seq[AircraftSpecificData[I, S]] = {

    val mapOfSeqs = sourceSeq.foldLeft(ListMap.empty[String, Seq[I]]) { case (map, element) =>
      val icao24 = element.icao24
      val seqToUpdate = map.getOrElse(icao24, Seq.empty[I])
      map.updated(icao24, seqToUpdate :+ element)
    }

    mapOfSeqs.values.toSeq.map { seqForAircraftSpecificData =>
      val seqForAircraftSpecificDataAsTypeS = seqForAircraftSpecificData.to(bf.toFactory(sourceSeq))
      new AircraftSpecificData(seqForAircraftSpecificDataAsTypeS)
    }
  }
}
