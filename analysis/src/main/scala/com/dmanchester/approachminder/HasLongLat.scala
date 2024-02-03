package com.dmanchester.approachminder

/**
 * Trait for classes that indicate a position on the Earth (or above it, but trait does not include
 * altitude information).
 */
trait HasLongLat {

  def longitude: Double

  def latitude: Double
}
