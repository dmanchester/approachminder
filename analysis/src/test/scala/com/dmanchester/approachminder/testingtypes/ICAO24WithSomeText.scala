package com.dmanchester.approachminder.testingtypes

import com.dmanchester.approachminder.positionreports.HasICAO24

case class ICAO24WithSomeText(icao24: String, text: String) extends HasICAO24
