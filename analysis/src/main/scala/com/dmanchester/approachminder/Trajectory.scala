package com.dmanchester.approachminder

/**
 * A sequence of at least two positions.
 *
 * A case class, so it has a usable `equals` method.
 * FIXME Does this have an apply method we need to suppress (since it doesn't verify invariant)? What's other case classes' status in that regard?
 *
 * TODO Could also make a regular class, force caller to check `positions` for equality.
 *
 * @param positions
 * @tparam A
 */
case class Trajectory[A] private(val positions: Seq[A])

object Trajectory {

  private def apply[A](positions: Seq[A]): Trajectory[A] = {
    // Suppress auto-generated "apply" method (would circumvent class's invariants)
    throw new UnsupportedOperationException
  }

  def newOption[A](positions: Seq[A]): Option[Trajectory[A]] = {
    Option.when(positions.length >= 2)(new Trajectory(positions))
  }
}