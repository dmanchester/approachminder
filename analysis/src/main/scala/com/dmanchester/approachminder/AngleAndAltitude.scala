package com.dmanchester.approachminder

class AngleAndAltitude private(val angle: PolarAngle, val altitudeMeters: Double)

object AngleAndAltitude {
  def apply(angle: PolarAngle, altitudeMeters: Double): AngleAndAltitude = new AngleAndAltitude(angle, altitudeMeters)
}