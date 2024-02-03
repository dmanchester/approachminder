package com.dmanchester.approachminder

/**
 * A time-based position of an aircraft, derived from an OpenSky vector. Includes the vector.
 *
 * @param timePosition
 * @param longitude
 * @param latitude
 * @param vector
 */
class TimeBasedPosition private(val timePosition: BigInt, val longitude: Double, val latitude: Double, val altitudeMeters: Double, val vector: StateVector) extends HasLongLatAlt with HasTime {
  // TODO While "timePosition" is consistent with OpenSky naming, simplify in this class to "time"?
  override def toString: String = s"${this.getClass.getSimpleName}(timePosition:$timePosition,longitude:$longitude,latitude:$latitude,altitudeMeters:$altitudeMeters,vector:$vector)"
}

object TimeBasedPosition {

  def apply(timePosition: BigInt, longitude: Double, latitude: Double, altitudeMeters: Double, vector: StateVector): TimeBasedPosition = new TimeBasedPosition(timePosition, longitude, latitude, altitudeMeters, vector)

  def option(vector: StateVector): Option[TimeBasedPosition] = {

    vector match {
      case StateVector(_, _, _, Some(timePosition), _, Some(longitude), Some(latitude), _, _, _, _, _, Some(geoAltitude), _, _, _, _) => Some(TimeBasedPosition(timePosition, longitude.toDouble, latitude.toDouble, geoAltitude.toDouble, vector))
      case _ => None
    }
  }
}
