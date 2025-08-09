package com.dmanchester.approachminder

import com.dmanchester.approachminder.typeswithbehavior.PolarAngle

class PolarAngleAndRelativePosition private(val angle: PolarAngle, val relativePosition: Double)

// TODO Further renaming here? Of fields?
object PolarAngleAndRelativePosition {
  def apply(angle: PolarAngle, relativePosition: Double): PolarAngleAndRelativePosition = new PolarAngleAndRelativePosition(angle, relativePosition)
}