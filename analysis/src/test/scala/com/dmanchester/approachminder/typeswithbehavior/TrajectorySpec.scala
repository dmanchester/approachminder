package com.dmanchester.approachminder.typeswithbehavior

import com.dmanchester.approachminder.typeswithoutbehavior.LighterThanAir
import org.specs2.mutable.*

class TrajectorySpec extends Specification {

  private val trajectory = Trajectory.newOption(Seq(6, 11, 4, 12, 3, 13), "icao24", Some("callsign"), Some(LighterThanAir)).get  // Fun fact: a Trajectory can contain non-longitude/latitude data
  private val predicateGreaterThan10: Int => Boolean = { _ > 10 }

  "truncateWhere" should {

    "truncate if a position matching the predicate is found" in {
      val truncatedTrajectory = trajectory.truncateWhere(1, predicateGreaterThan10)
      truncatedTrajectory mustEqual Trajectory.newOption(Seq(6, 11, 4), "icao24", Some("callsign"), Some(LighterThanAir)).get
    }

    "do nothing if a position matching the predicate isn't found" in {
      val predicate: Int => Boolean = { _ > 20 }
      val truncatedTrajectory = trajectory.truncateWhere(1, predicate)
      truncatedTrajectory mustEqual trajectory
    }

    "do nothing if the last position is specified" in {
      val truncatedTrajectory = trajectory.truncateWhere(trajectory.positions.length - 1, predicateGreaterThan10)
      truncatedTrajectory mustEqual trajectory
    }

    "throw on positionIndex too low" in {
      trajectory.truncateWhere(0, predicateGreaterThan10) must throwA[IndexOutOfBoundsException]
    }

    "throw on positionIndex too high" in {
      trajectory.truncateWhere(trajectory.positions.length, predicateGreaterThan10) must throwA[IndexOutOfBoundsException]
    }
  }
}