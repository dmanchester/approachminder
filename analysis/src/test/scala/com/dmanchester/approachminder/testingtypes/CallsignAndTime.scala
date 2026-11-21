package com.dmanchester.approachminder.testingtypes

import com.dmanchester.approachminder.io.HasCallsignAndTime

case class CallsignAndTime(callsign: Option[String], timePosition: BigInt) extends HasCallsignAndTime
