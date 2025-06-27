package com.dmanchester.approachminder.testingtypes

import com.dmanchester.approachminder.typeswithoutbehavior.HasCallsignAndTime

case class CallsignAndTime(callsign: Option[String], timePosition: BigInt) extends HasCallsignAndTime
