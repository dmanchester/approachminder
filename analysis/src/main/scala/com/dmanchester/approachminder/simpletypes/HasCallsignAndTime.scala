package com.dmanchester.approachminder.simpletypes

import com.dmanchester.approachminder.HasTime

trait HasCallsignAndTime extends HasTime {  // TODO Confirm there's upside to extending this other trait.
  def callsign: Option[String]
  def timePosition: BigInt
}
