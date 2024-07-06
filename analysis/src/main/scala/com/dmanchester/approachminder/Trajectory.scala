package com.dmanchester.approachminder

/**
 * A sequence of at least two positions.
 *
 * A case class, so it has a usable `equals` method.
 *
 * @param positions
 * @tparam A
 */
case class Trajectory[A] private(val positions: Seq[A])

object Trajectory {

  def newOption[A](positions: Seq[A]): Option[Trajectory[A]] = {
    Option.when(positions.length >= 2)(new Trajectory(positions))
  }
}