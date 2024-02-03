package com.dmanchester.approachminder

import Airports.sfo

import org.locationtech.jts.algorithm.Angle
import org.locationtech.jts.geom.Coordinate

object ThrowawayMain2 {

  def main(args: Array[String]): Unit = {

    val coordinate = new Coordinate(-1, -0.0001)
    println(s"Angle: ${Angle.angle(coordinate)}")

//    val referencePoint = LongLat(-122, 38)
////    val pointR = LongLat(-122, 40.7) // 299.7 km; between rings #4 and #5
////    val pointS = LongLat(-121.9, 40.3) // 255.4 km; between rings #3 and #4
////    val pointT = LongLat(-121.9, 40.1) // 233.2 km; also between rings #3 and #4
////    val pointU = LongLat(-122.1, 39.2) // 133.5 km; between rings #1 and #2 (no points between #2 and #3)
////    val pointV = LongLat(-122.1, 38.5) // 56.2 km; inside ring #1
////
////    val points = Seq(pointV, pointU, pointT, pointS, pointR)
//      val points = Seq(LongLat(-122.100010, 38.625875), LongLat(-122.086899, 39.259719), LongLat(-121.946771, 39.891761), LongLat(-121.955503, 40.522587))
//
//    points.foreach { point =>
//      val distance = sfo.geographicCalculator.distanceInMeters(referencePoint, point)
//      println(s"distance: $distance")
//    }
  }
}
