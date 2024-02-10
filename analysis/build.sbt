ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "2.13.12"

lazy val root = (project in file("."))
  .settings(
    name := "analysis",
    scalacOptions ++= Seq(
      "-deprecation"
    ),
    libraryDependencies ++= Seq(
      // TODO Clean up versions etc.
      "org.scala-lang.modules" %% "scala-xml" % "2.2.0",  // was 2.1.0
      "com.typesafe.play" %% "play-json" % "2.10.3",  // was 2.9.3; 3.x versions also available, but not in Maven Central?
      "com.typesafe.scala-logging" %% "scala-logging" % "3.9.5",  // was 3.9.4
      "ch.qos.logback" % "logback-classic" % "1.3.14",  // was 1.3.4
      //"javax.media" % "jai_core" % "1.1.3" from "https://repo.osgeo.org/repository/release/javax/media/jai_core/1.1.3/jai_core-1.1.3.jar",  // HACK
      "org.geotools" % "gt-shapefile" % "27.1" exclude("javax.media", "jai_core"),  // 30.1 available; TODO Document this craziness with "jai_core"
      "org.apache.commons" % "commons-math3" % "3.6.1",
      "org.specs2" %% "specs2-core" % "4.20.4" % "test"  // was 4.17.0; 5.x versions also available!
    ),
    resolvers += "OSGeo" at "https://repo.osgeo.org/repository/release/"  // TODO How to apply this resolver just to the one library?
  )
