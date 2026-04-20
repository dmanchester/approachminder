import {
  Cartesian3,
  ClockRange,
  Entity,
  ImageryLayer,
  IonImageryProvider,
  type IonResource,
  JulianDate,
  SampledPositionProperty,
  Terrain,
  VelocityOrientationProperty,
  Viewer
} from 'cesium';

import type Position from "./Position";
import type Trajectory from "./Trajectory";

// TODO This function's ImageryLayer.fromProviderAsync(IonImageryProvider.fromAssetId(...)) call (conditionally invoked)
// presumably has a precondition of Ion.defaultAccessToken being set. Its Terrain.fromWorldTerrain() call (always
// invoked) may, too. -- That precondition should be formalized. If it's a precondition of both calls, it probably makes
// sense to simply document it. -- However, if the precondition only applies to the ImageryLayer.fromProviderAsync()
// call, one way to deal with it would be to change this function's API to force calling code to meet the precondition:
// add an argument of "altBaseLayer?: Promise<ImageryProvider>". -- The IonImageryProvider.fromAssetId(3954) invocation
// would then move to the calling code, which would only perform it if Bing imagery *weren't* desired.
export function viewerOptions(useBingImagery: boolean): Viewer.ConstructorOptions {

  const options: Viewer.ConstructorOptions = {
    baseLayerPicker: false,
    geocoder: false,
    homeButton: false,
    sceneModePicker: false,
    terrain: Terrain.fromWorldTerrain()
    // Using "terrain: ..." instead of "terrainProvider: await createWorldTerrainAsync()" per:
    //
    // https://github.com/CesiumGS/cesium-webpack-example/blob/23638ff7ce845a655c949de9a01e765c91ee94ba/webpack-5/src/index.js#L17
  };

  if (!useBingImagery) {
    // Use Sentinel-2 imagery. See:
    //
    //   * https://sandcastle.cesium.com/?src=Sentinel-2.html
    //   * https://cesium.com/learn/ion/optimizing-quotas/
    options.baseLayer = ImageryLayer.fromProviderAsync(IonImageryProvider.fromAssetId(3954));
  }

  return options;
}

export function configureViewer(viewer: Viewer, startTime: JulianDate, stopTime: JulianDate, currentTime: JulianDate): void {
  viewer.clock.shouldAnimate = true;
  viewer.clock.startTime = startTime.clone();
  viewer.clock.stopTime = stopTime.clone();
  viewer.clock.currentTime = currentTime.clone();
  viewer.clock.clockRange = ClockRange.CLAMPED;
  viewer.timeline.zoomTo(startTime, stopTime);
}

export function createCesiumEntity(trajectory: Trajectory, airplaneIonResource: IonResource): Entity {

  const times = trajectory.positions.map(position => position.time);
  const positions = trajectory.positions.map(position => Cartesian3.fromDegrees(position.longitude, position.latitude, position.altitude));

  const positionProperty = new SampledPositionProperty();
  positionProperty.addSamples(times, positions);

  return new Entity({
    name: trajectory.callsign ?? "null",
    // TODO Add "availability"? Along lines of... availability: new Cesium.TimeIntervalCollection([ new Cesium.TimeInterval({ start: start, stop: stop }) ])
    position: positionProperty,
    model: { uri: airplaneIonResource },
    // Automatically compute the orientation from the position.
    orientation: new VelocityOrientationProperty(positionProperty)
  });
}

/**
 * Wraps a Position and adds a UI-oriented field: the age of the position (relative to a current point in time).
 */
export type PositionWrapper = ({
  position: Position,
  ageSecs: number
});

/**
 * Formats a number, showing a specified count of digits after the decimal point. As needed: rounds/right-pads with
 * zeroes; includes thousands separators.
 *
 * Uses U.S.-style decimal point and thousands separator.
 *
 * If null or undefined is passed, returns "null" or "undefined", respectively.
 *
 * @param value The number to format.
 * @param fractionDigits How many digits to show after the decimal point.
 */
export function formatNumber(value: number | null | undefined, fractionDigits: number): string {

  if (value === null) {
    return "null";
  }

  if (value === undefined) {
    return "undefined";
  }

  const numberFormat = new Intl.NumberFormat("en-US", {
    minimumFractionDigits: fractionDigits,
    maximumFractionDigits: fractionDigits
  });

  return numberFormat.format(value);
}
