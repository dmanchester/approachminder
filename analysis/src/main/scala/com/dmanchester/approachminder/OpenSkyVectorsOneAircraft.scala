package com.dmanchester.approachminder

import scala.collection.BuildFrom
import scala.collection.immutable.ListMap

case class OpenSkyVectorsOneAircraft[S[_] <: Seq[_]] private (vectors: S[OpenSkyVector])
// TODO Which of the above OpenSkyVector can be replaced with _?

object OpenSkyVectorsOneAircraft {
  /**
   * Groups a multi-aircraft `Seq` of OpenSky vectors by `icao24` value, producing an `OpenSkyVectorsOneAircraft` for
   * each one.
   *
   * Each `OpenSkyVectorsOneAircraft` instance's vectors retain their ordering from the source `Seq`.
   *
   * Additionally, the `OpenSkyVectorsOneAircraft` instances themselves are ordered by when an `icao24` value first
   * appeared in the source `Seq`.
   *
   * @param sourceSeq
   * @param bf
   * @tparam I
   * @tparam S
   * @return
   */
//  def createMultiple[S[_] <: Seq[_]](vectorsMultipleAircraft: S[OpenSkyVector])(implicit bf: BuildFrom[S[OpenSkyVector], OpenSkyVector, S[OpenSkyVector]]): Seq[OpenSkyVectorsOneAircraft[S]] = {
//// TODO Work on type signature
//
//
//// DAN YOU LEFT OFF HERE. BLECH, IN THE WEEDS.
//
//    val mapOfSeqs = vectorsMultipleAircraft.foldLeft(ListMap.empty[String, Seq[OpenSkyVector]]) { case (map, vector: OpenSkyVector) =>
//      val icao24 = element.icao24
//      val seqToUpdate = map.getOrElse(icao24, Seq.empty[OpenSkyVector])
//      map.updated(icao24, seqToUpdate :+ element)
//    }
//
//    mapOfSeqs.values.toSeq.map { seqForAircraftSpecificData =>
//      val seqForAircraftSpecificDataAsTypeS = seqForAircraftSpecificData.to(bf.toFactory(sourceSeq))
//      new AircraftSpecificData(seqForAircraftSpecificDataAsTypeS)
//    }
//  }
}