package com.dmanchester.approachminder

import org.geotools.measure.Units

/**
 * Utility functions that operate on standard Scala datatypes.
 */
object Utils {
  val feetToMetersConverter = Units.FOOT.getConverterTo(Units.METRE)
}
