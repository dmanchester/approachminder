package com.dmanchester.approachminder

class LongLatAlt private(val longitude: Double, val latitude: Double, val altitudeMeters: Double) extends HasLongLatAlt {
  override def toString = s"${this.getClass.getSimpleName}($longitude,$latitude,$altitudeMeters)" // styled after case classes' toString
}

object LongLatAlt {
  def apply(longitude: Double, latitude: Double, altitudeMeters: Double): LongLatAlt = new LongLatAlt(longitude, latitude, altitudeMeters)
}