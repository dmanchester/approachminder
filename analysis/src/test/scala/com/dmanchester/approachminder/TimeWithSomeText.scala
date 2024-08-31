package com.dmanchester.approachminder

case class TimeWithSomeText(timePosition: BigInt, text: String = "" /* Do I rely anywhere on the default value? */) extends HasTime
