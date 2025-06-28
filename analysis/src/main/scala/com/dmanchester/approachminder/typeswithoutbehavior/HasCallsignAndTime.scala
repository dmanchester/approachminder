package com.dmanchester.approachminder.typeswithoutbehavior

import com.dmanchester.approachminder.HasTime

trait HasCallsignAndTime extends HasTime {  // TODO Confirm there's upside to extending HasTime here.
  def callsign: Option[String]
  def timePosition: BigInt
}
