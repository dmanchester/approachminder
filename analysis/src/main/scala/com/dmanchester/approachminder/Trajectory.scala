package com.dmanchester.approachminder

/**
 * A sequence of at least two positions.
 *
 * A case class, so it has a usable `equals` method.
 * FIXME Below attempt to suppress apply method doesn't seem to be working! Also, what else do we need to suppress? And, what's other case classes' status in this regard?
 *
 * TODO Could also make a regular class, force caller to check `positions` for equality.
 *
 * @param positions
 * @tparam A
 */
case class Trajectory[A] private(val positions: Seq[A]) {
  def drop(n: Int): Option[Trajectory[A]] = Trajectory.newOption(positions.drop(n))

  def isSegmentIndexValid(index: Int): Boolean = {
    index >= 0 && index <= (positions.length - 2)  // n positions constitute (n - 1) segments; with zero-based indexing, last segment's index is (n - 2)
  }
}

object Trajectory {

  private def apply[A](positions: Seq[A]): Trajectory[A] = {
    // Suppress auto-generated "apply" method (would circumvent class's invariants)
    throw new UnsupportedOperationException
  }

  def newOption[A](positions: Seq[A]): Option[Trajectory[A]] = {
    Option.when(positions.length >= 2)(new Trajectory(positions))
  }
}