package com.dmanchester.approachminder

import com.dmanchester.approachminder.typeswithoutbehavior.HasTime

case class TimeWithSomeText(timePosition: BigInt, text: String = "" /* Do I rely anywhere on the default value? */) extends HasTime
