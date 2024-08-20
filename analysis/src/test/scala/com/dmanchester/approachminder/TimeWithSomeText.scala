package com.dmanchester.approachminder

class Time private(val timePosition: BigInt) extends HasTime

object Time {
  def applyZ(timePosition: BigInt): Time = new Time(timePosition)
}