package com.dmanchester.approachminder

class TimeBasedPositionWithApproachSegment private(val timeBasedPosition: TimeBasedPosition, val approachSegment: Option[ApproachSegmentWithDeviation])

object TimeBasedPositionWithApproachSegment {
  def apply(timeBasedPosition: TimeBasedPosition, approachSegment: Option[ApproachSegmentWithDeviation]): TimeBasedPositionWithApproachSegment = new TimeBasedPositionWithApproachSegment(timeBasedPosition, approachSegment)
}