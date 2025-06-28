package com.dmanchester.approachminder.typeswithoutbehavior

trait HasCallsignAndTime extends HasTime {  // TODO Confirm there's upside to extending HasTime here.
  def callsign: Option[String]
  def timePosition: BigInt
}
