package com.dmanchester.approachminder

/**
 * A position on the Earth (or above it, but class does not include altitude information).
 *
 * Isn't a case class because the case-class equals() isn't suitable when one or more fields are
 * floating-point numbers (Double etc.).
 *
 * @param longitude
 * @param latitude
 */
class LongLat private(val longitude: Double, val latitude: Double) extends HasLongLat {
  override def toString = s"${this.getClass.getSimpleName}($longitude,$latitude)" // styled after case classes' toString
}

object LongLat {
  def apply(longitude: Double, latitude: Double) = new LongLat(longitude, latitude)
}