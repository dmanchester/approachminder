package com.dmanchester.approachminder

import com.dmanchester.approachminder.Airports.sfo

object ThrowawayRunwayCenterline {

  def main(args: Array[String]): Unit = {
    val sfoThreshold10L = sfo.thresholdByName("10L").get

    val relativePositions = Seq(-0.3, -0.15, 0.15, 0.3, 0.5, 0.7, 0.9, 1.1)
    relativePositions.foreach { pos =>
      val point = sfoThreshold10L.pointOnRunwayCenterline(pos)
      println(point)
    }
  }
}
