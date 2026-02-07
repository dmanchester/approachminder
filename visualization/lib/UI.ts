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

  const theOptions: Viewer.ConstructorOptions = {
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
    theOptions.baseLayer = ImageryLayer.fromProviderAsync(IonImageryProvider.fromAssetId(3954));
  }

  return theOptions;
}

export function configureViewer(viewer: Viewer, start: JulianDate, stop: JulianDate): void {
  // const start = trajectories.earliestTime();
  // const stop = trajectories.latestTime();
  viewer.clock.shouldAnimate = true;
  viewer.clock.startTime = start.clone();
  viewer.clock.stopTime = stop.clone();
  viewer.clock.currentTime = start.clone();
  viewer.clock.clockRange = ClockRange.CLAMPED;
  viewer.timeline.zoomTo(start, stop);
}

function createCesiumEntity(trajectory: Trajectory, airplaneIonResource: IonResource): Entity {

  const times = trajectory.timeBasedPositions.map(timeBasedPosition => timeBasedPosition.time);
  const positions = trajectory.timeBasedPositions.map(timeBasedPosition => Cartesian3.fromDegrees(timeBasedPosition.longitude, timeBasedPosition.latitude, timeBasedPosition.altitude));

  const positionProperty = new SampledPositionProperty();
  positionProperty.addSamples(times, positions);

  return new Entity({
    name: trajectory.aircraftProfile.callsign ?? "null",
    //  availability: new Cesium.TimeIntervalCollection([ new Cesium.TimeInterval({ start: start, stop: stop }) ]),
    position: positionProperty,
    model: { uri: airplaneIonResource },
    // Automatically compute the orientation from the position.
    orientation: new VelocityOrientationProperty(positionProperty)
  });
}

export function createCesiumEntities(trajectories: Array<Trajectory>, airplaneIonResource: IonResource): Map<Trajectory, Entity> {

  const trajectoriesToEntities = new Map<Trajectory, Entity>();

  trajectories.forEach(trajectory => {
    const entity = createCesiumEntity(trajectory, airplaneIonResource);
    trajectoriesToEntities.set(trajectory, entity);
  });

  return trajectoriesToEntities;
}