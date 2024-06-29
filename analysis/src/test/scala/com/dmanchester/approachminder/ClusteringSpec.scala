package com.dmanchester.approachminder

import org.specs2.mutable._
import SharedResources._

import com.dmanchester.approachminder.Airports.sfo

class ClusteringSpec extends Specification {

  "cluster" should {

    "cluster" in {

      val easterlyPoints = constructAnglesAndAltitudes(Seq(
        88.1,
        89.2,
        90.3,
        91.4,
        92.5,
        93.6
      ))

      val southerlyPoints = constructAnglesAndAltitudes(Seq(
        177.1,
        178.2,
        179.3,
        180.4,
        181.5,
        182.6,
        183.7
      ))

      val westerlyPoints = constructAnglesAndAltitudes(Seq(
        267.1,
        268.2,
        269.3,
        270.4,
        271.5,
        272.6,
        273.7,
        274.8
      ))

      val northerlyPoints = constructAnglesAndAltitudes(Seq(  // by design, not enough points to form a cluster
        359.1,
        0.2,
        1.3,
      ))

      // Place the points in a randomized order.
      val points = Seq(
        westerlyPoints(0),
        easterlyPoints(2),
        westerlyPoints(7),
        easterlyPoints(3),
        westerlyPoints(3),
        southerlyPoints(3),
        northerlyPoints(0),
        southerlyPoints(5),
        easterlyPoints(1),
        northerlyPoints(2),
        westerlyPoints(5),
        easterlyPoints(4),
        westerlyPoints(4),
        easterlyPoints(5),
        southerlyPoints(1),
        southerlyPoints(2),
        westerlyPoints(1),
        southerlyPoints(0),
        southerlyPoints(4),
        southerlyPoints(6),
        northerlyPoints(1),
        westerlyPoints(6),
        westerlyPoints(2),
        easterlyPoints(0)
      )

      val distanceInMeters = 1000.0
      val minPointsPerCluster = 4
      val eps = 500.0
      // with minPointsPerCluster = 4, with an eps of...
      //
      // 10 gives zero clusters
      // 100, 500, 1000 gives three clusters (8, 6, 7)  :) :) :)
      // 10000 gives one cluster

      // with minPointsPerCluster = 6, with an eps of...
      //
      // 0.1, 1, 10, 50 gives zero clusters
      // 75, 100, 500, 1000 gives two clusters (8, 7)
      // 1500, 2000, 5000, 10000 gives one cluster (24)

      val clusters = Clustering.cluster(points, sfo.referencePoint, distanceInMeters, sfoCalculator, eps, minPointsPerCluster)

      // TODO Commented-out code facilitates a visual check that clusters' contents are correct. Turn into an automated
      // test.
      //
//      clusters.foreach { cluster =>
//        println("Cluster:")
//        cluster.foreach { point =>
//          println(s"  ${point.angle.toCompassDegrees}")
//        }
//      }

      clusters.length must beEqualTo(3)
    }
  }

  /**
   * Constructs `AngleAndAltitude` instances from angles (representing compass degrees). For altitude component,
   * arbitrarily re-uses angle value as a meters value and doubles it. (So, for example, the point at 270 deg. is given
   * an altitude of 540 m.)
   *
   * @param angles
   * @return
   */
  def constructAnglesAndAltitudes(angles: Seq[Double]): Seq[AngleAndAltitude] = {
    angles.map { angle =>
      val polarAngle = PolarAngle.fromCompassDegrees(angle)
      val altitude = 2.0 * angle
      AngleAndAltitude(polarAngle, altitude)
    }
  }
}