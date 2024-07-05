package com.dmanchester.approachminder

import org.apache.commons.math3.ml.clustering.{Clusterable, DBSCANClusterer}
import org.apache.commons.math3.ml.distance.DistanceMeasure
import scala.jdk.CollectionConverters._

object Clustering {

  def cluster(anglesAndAltitudes: Seq[AngleAndAltitude], origin: HasLongLat, distanceInMeters: Double, geographicCalculator: GeographicCalculator, epsDistanceMeters: Double, minPointsPerCluster: Int): Seq[Seq[AngleAndAltitude]] = {

    val positions = anglesAndAltitudes.map { angleAndAltitude =>
      val position2D = geographicCalculator.pointAtAngleAndDistance(origin, angleAndAltitude.angle, distanceInMeters)
      LongLatAlt(position2D.longitude, position2D.latitude, angleAndAltitude.altitudeMeters)
    }

    val distanceMeasure = LongLatAltDistanceMeasure(positions, geographicCalculator)

    val clusterer = new DBSCANClusterer[IndexedClusterable](epsDistanceMeters, minPointsPerCluster, distanceMeasure)  // https://commons.apache.org/proper/commons-math/javadocs/api-3.6.1/index.html?org/apache/commons/math3/ml/clustering/DBSCANClusterer.html

    val clusters = clusterer.cluster(IndexedClusterable.instances(positions.length).asJava).asScala.toSeq

    clusters.map { cluster =>
      cluster.getPoints.asScala.toSeq.map { indexedClusterable =>
        anglesAndAltitudes(indexedClusterable.index)
      }
    }
  }
}

class IndexedClusterable private(val index: Int) extends Clusterable {
  override def getPoint: Array[Double] = Array(index.toDouble)
}

object IndexedClusterable {
  def apply(index: Int): IndexedClusterable = new IndexedClusterable(index)

  def instances(count: Int): Seq[IndexedClusterable] = {
    for (i <- 0 until count)
      yield IndexedClusterable(i)
  }
}

class LongLatAltDistanceMeasure private(val positions: Seq[LongLatAlt], geographicCalculator: GeographicCalculator) extends DistanceMeasure {
  override def compute(aIndex: Array[Double], bIndex: Array[Double]): Double = {
    val a = positions(aIndex(0).toInt)
    val b = positions(bIndex(0).toInt)

    val distance2D = geographicCalculator.distanceInMeters(a, b)
    val altitudeDifference = a.altitudeMeters - b.altitudeMeters

    MathUtils.hypotenuseLength(distance2D, altitudeDifference)
  }
}

object LongLatAltDistanceMeasure {
  def apply(positions: Seq[LongLatAlt], geographicCalculator: GeographicCalculator): LongLatAltDistanceMeasure = new LongLatAltDistanceMeasure(positions, geographicCalculator)
}
