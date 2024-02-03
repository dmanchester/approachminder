package com.dmanchester.approachminder

class PolarAngleAndRelativePosition private(val angle: PolarAngle, val relativePosition: Double)

// TODO Further renaming here? Of fields?
object PolarAngleAndRelativePosition {
  def apply(angle: PolarAngle, relativePosition: Double): PolarAngleAndRelativePosition = new PolarAngleAndRelativePosition(angle, relativePosition)
}