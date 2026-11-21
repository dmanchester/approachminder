package com.dmanchester.approachminder.io

trait HasCallsignAndTime extends HasTime {  // TODO Confirm there's upside to extending HasTime here.
  def callsign: Option[String]
  def timePosition: BigInt
}
