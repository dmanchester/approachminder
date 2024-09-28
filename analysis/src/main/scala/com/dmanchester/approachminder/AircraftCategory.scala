package com.dmanchester.approachminder

sealed abstract class AircraftCategory(val description: String)

// Data from https://openskynetwork.github.io/opensky-api/rest.html.
case object NoInfoAtAll extends AircraftCategory("No information at all")
case object NoADSBEmitterCategoryInfo extends AircraftCategory("No ADS-B Emitter Category Information")
case object Light extends AircraftCategory("Light (< 15500 lbs)")
case object Small extends AircraftCategory("Small (15500 to 75000 lbs)")
case object Large extends AircraftCategory("Large (75000 to 300000 lbs)")
case object HighVortexLarge extends AircraftCategory("High Vortex Large (aircraft such as B-757)")
case object Heavy extends AircraftCategory("Heavy (> 300000 lbs)")
case object HighPerformance extends AircraftCategory("High Performance (> 5g acceleration and 400 kts)")
case object Rotorcraft extends AircraftCategory("Rotorcraft")
case object GliderOrSailplane extends AircraftCategory("Glider / sailplane")
case object LighterThanAir extends AircraftCategory("Lighter-than-air")
case object ParachutistOrSkydiver extends AircraftCategory("Parachutist / Skydiver")
case object UltralightOrHanggliderOrParaglider extends AircraftCategory("Ultralight / hang-glider / paraglider")
case object Reserved extends AircraftCategory("Reserved")
case object UnmannedAerialVehicle extends AircraftCategory("Unmanned Aerial Vehicle")
case object SpaceOrTransatmosphericVehicle extends AircraftCategory("Space / Trans-atmospheric vehicle")
case object SurfaceVehicleEmergencyVehicle extends AircraftCategory("Surface Vehicle - Emergency Vehicle")
case object SurfaceVehicleServiceVehicle extends AircraftCategory("Surface Vehicle - Service Vehicle")
case object PointObstacle extends AircraftCategory("Point Obstacle (includes tethered balloons)")
case object ClusterObstacle extends AircraftCategory("Cluster Obstacle")
case object LineObstacle extends AircraftCategory("Line Obstacle")

object AircraftCategory {

  val byId: Map[Int, AircraftCategory] = Map(
    0 -> NoInfoAtAll,
    1 -> NoADSBEmitterCategoryInfo,
    2 -> Light,
    3 -> Small,
    4 -> Large,
    5 -> HighVortexLarge,
    6 -> Heavy,
    7 -> HighPerformance,
    8 -> Rotorcraft,
    9 -> GliderOrSailplane,
    10 -> LighterThanAir,
    11 -> ParachutistOrSkydiver,
    12 -> UltralightOrHanggliderOrParaglider,
    13 -> Reserved,
    14 -> UnmannedAerialVehicle,
    15 -> SpaceOrTransatmosphericVehicle,
    16 -> SurfaceVehicleEmergencyVehicle,
    17 -> SurfaceVehicleServiceVehicle,
    18 -> PointObstacle,
    19 -> ClusterObstacle,
    20 -> LineObstacle
  )

  val blank: Set[AircraftCategory] = Set(
    NoInfoAtAll,
    NoADSBEmitterCategoryInfo
  )

  val fixedWingPowered: Set[AircraftCategory] = Set(
    Light,
    Small,
    Large,
    HighVortexLarge,
    Heavy,
    HighPerformance,
    UnmannedAerialVehicle
  )

  /**
   * TODO Move to Utils?
   *
   * In a collection of aircraft categories, determine the most-common one that is "non-blank" (i.e., not `NoInfoAtAll`
   * or `NoADSBEmitterCategoryInfo`). The collection cannot be empty.
   *
   * If multiple categories are equally common, pick the one that is alphabetically first by class name. (This is just
   * to ensure deterministic behavior regardless of the categories' ordering.)
   *
   * @param categories
   * @throws java.lang.UnsupportedOperationException If collection is empty.
   * @return the most-common `AircraftCategory` as a `Some`; or, `None` if all categories are
   *         `NoInfoAtAll`/`NoADSBEmitterCategoryInfo`.
   */
  @throws(classOf[UnsupportedOperationException])
  def mostCommonNonBlankCategoryInNonEmptyCollection(categories: Iterable[AircraftCategory]): Option[AircraftCategory] = {

    if (categories.isEmpty) {
      throw new UnsupportedOperationException("'categories' must not be empty!")
    }

    val nonBlankCategories = categories.filter(!AircraftCategory.blank.contains(_))
    if (nonBlankCategories.isEmpty) {
      None
    } else {
      val category = Utils.mostCommonValueInNonEmptyCollection(nonBlankCategories) { (a, b) => a.getClass.getSimpleName < b.getClass.getSimpleName }
      Some(category)
    }
  }
}