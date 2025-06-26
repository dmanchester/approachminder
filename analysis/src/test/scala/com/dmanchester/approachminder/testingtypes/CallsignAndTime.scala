package com.dmanchester.approachminder.testingtypes

import com.dmanchester.approachminder.simpletypes.HasCallsignAndTime

case class CallsignAndTime(callsign: Option[String], timePosition: BigInt) extends HasCallsignAndTime
