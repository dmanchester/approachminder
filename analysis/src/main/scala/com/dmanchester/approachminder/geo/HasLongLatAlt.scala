package com.dmanchester.approachminder.geo

trait HasLongLatAlt extends HasLongLat {
  def altitudeMeters: Double
}
