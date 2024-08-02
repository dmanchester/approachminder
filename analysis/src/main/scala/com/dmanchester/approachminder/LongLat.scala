package com.dmanchester.approachminder

/**
 * A position on the Earth (or above it, but class does not include altitude information).
 *
 * @param longitude
 * @param latitude
 */
case class LongLat(longitude: Double, latitude: Double) extends HasLongLat