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

export function configureViewer(viewer: Viewer, start: JulianDate, stop: JulianDate): void {
  viewer.clock.shouldAnimate = true;
  viewer.clock.startTime = start.clone();
  viewer.clock.stopTime = stop.clone();
  viewer.clock.currentTime = start.clone();
  viewer.clock.clockRange = ClockRange.CLAMPED;
  viewer.timeline.zoomTo(start, stop);
}

function createCesiumEntity(trajectory: Trajectory, airplaneIonResource: IonResource): Entity {

  const times = trajectory.positions.map(position => position.time);
  const positions = trajectory.positions.map(position => Cartesian3.fromDegrees(position.longitude, position.latitude, position.altitude));

  const positionProperty = new SampledPositionProperty();
  positionProperty.addSamples(times, positions);

  return new Entity({
    name: trajectory.aircraftProfile.callsign ?? "null",
    // TODO Add "availability"? Along lines of... availability: new Cesium.TimeIntervalCollection([ new Cesium.TimeInterval({ start: start, stop: stop }) ])
    position: positionProperty,
    model: { uri: airplaneIonResource },
    // Automatically compute the orientation from the position.
    orientation: new VelocityOrientationProperty(positionProperty)
  });
}

export function createCesiumEntities(trajectories: Array<Trajectory>, airplaneIonResource: IonResource): Map<Trajectory, Entity> {
  const trajectoriesToEntities: Array<[Trajectory, Entity]> = trajectories.map(trajectory => [trajectory, createCesiumEntity(trajectory, airplaneIonResource)]);
  return new Map(trajectoriesToEntities);
}