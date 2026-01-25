<script lang="ts">
  import { onMount } from 'svelte';
  import { SplitPane } from '@rich_harris/svelte-split-pane';
  import AircraftTable from './AircraftTable.svelte';
  import {
    Cartesian3,
    ClockRange,
    Entity,
    ImageryLayer,
    Ion,
    IonResource,
    IonImageryProvider,
    JulianDate,
    SampledPositionProperty,
    Terrain,
    VelocityOrientationProperty,
    Viewer,
  } from 'cesium';
  import '../node_modules/cesium/Source/Widgets/widgets.css';  // TODO Compare with "import 'cesium/Build/Cesium/Widgets/widgets.css'"; and, what do I get from this?

  import IO from '../lib/IO';
  import type { Observation } from '../lib/Observation';
  import type Trajectory from '../lib/Trajectory';
  import trajectoriesFromJSON from './data.json';
  import approachMinderConfig from '../approachminder-config.json';

  import { sortBy } from 'lodash';

  Ion.defaultAccessToken = approachMinderConfig.cesiumIon.accessToken;

  const start = JulianDate.fromIso8601('2022-12-06T18:49:09Z');
  const stop = JulianDate.fromIso8601('2022-12-06T18:56:01Z');
  const maxThresholdDistanceMetersForApproach = 10000;
  const windowDuration = 60;  // seconds
  const firstCallsignToTrack = 'SKW4081';

  const urlParams = new URLSearchParams(window.location.search);
  const useBingImagery = urlParams.get('bing') === 'true';

  const trajectories = IO.trajectoriesFromParsedJSON(trajectoriesFromJSON, JulianDate.fromIso8601);
  const trajectoriesToEntities = new Map<Trajectory, Entity>();

  let viewer: Viewer;
  let observationsAircraftOnApproach: Array<Observation> = [];
  let observationsOtherAircraft: Array<Observation> = [];

  onMount(async () => {

    // TODO What code that's currently in this function can be moved before it, to improve startup performance?

    try {
      const viewerOptions: Viewer.ConstructorOptions = {
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
        viewerOptions.baseLayer = ImageryLayer.fromProviderAsync(IonImageryProvider.fromAssetId(3954));
      }

      viewer = new Viewer('cesiumContainer', viewerOptions);

    } catch (err) {
       // TODO Error is likely fatal. Better to not catch and allow to propagate?
      console.log(err);
    }

    // const start = trajectories.earliestTime();
    // const stop = trajectories.latestTime();
    viewer.clock.shouldAnimate = true;
    viewer.clock.startTime = start.clone();
    viewer.clock.stopTime = stop.clone();
    viewer.clock.currentTime = start.clone();
    viewer.clock.clockRange = ClockRange.CLAMPED;
    viewer.timeline.zoomTo(start, stop);

    const airplaneUri = await IonResource.fromAssetId(approachMinderConfig.cesiumIon.assetIdAirplane);

    trajectories.theTrajectories.forEach(trajectory => {

      const times = trajectory.timeBasedPositions.map(timeBasedPosition => timeBasedPosition.time);
      const positions = trajectory.timeBasedPositions.map(timeBasedPosition => Cartesian3.fromDegrees(timeBasedPosition.longitude, timeBasedPosition.latitude, timeBasedPosition.altitude));

      const positionProperty = new SampledPositionProperty();
      positionProperty.addSamples(times, positions);

      const entity = new Entity({
        name: trajectory.aircraftProfile.callsign ?? "null",
        //  availability: new Cesium.TimeIntervalCollection([ new Cesium.TimeInterval({ start: start, stop: stop }) ]),
        position: positionProperty,
        model: { uri: airplaneUri },
        // Automatically compute the orientation from the position.
        orientation: new VelocityOrientationProperty(positionProperty)
      });

      trajectoriesToEntities.set(trajectory, entity);
    });

    for (const entity of trajectoriesToEntities.values()) {
       viewer.entities.add(entity);
    }

    // const firstTrajectory = trajectories.theTrajectories[0];  // TODO This is hacky. Also, concern ourselves with no-entities case?
    const trajectoryToTrack = trajectories.theTrajectories.find(trajectory => trajectory.aircraftProfile.callsign === firstCallsignToTrack)!;
    viewer.trackedEntity = trajectoriesToEntities.get(trajectoryToTrack);
    // viewer.clock.currentTime = firstTrajectory.earliestTime().clone();

    let lastTimeProcessed: JulianDate | null = null;  // TODO Switched from "undefined" to "null". Any other explicit "undefined" in codebase?

    viewer.clock.onTick.addEventListener(() => {  // Whoa, this gets called all the time, even when clock is stopped!

      const time = viewer.clock.currentTime;

      if ((lastTimeProcessed !== null) && JulianDate.equals(time, lastTimeProcessed)) {
        // Nothing to do.
        return;
      }

      // Get the latest positions within the time window, one per aircraft.
      const latestPositionsWithinWindow = trajectories.latestPositionsWithinWindow(time, windowDuration);  // TODO Rather than a tuple, this function could return an Object that names the two members (I end up doing this below anyway)
      // TODO First time I've used the "observations" terminology. If it sticks, broaden back to the Scala code?
      const observations = latestPositionsWithinWindow.map(([trajectory, timeBasedPosition]) => ({
        trajectory: trajectory,
        position: timeBasedPosition,
        ageOfObservation: Math.round(JulianDate.secondsDifference(time, timeBasedPosition.time))
      }));
      const observationsAircraftOnApproachUnsorted = observations.filter(observation => {
        const approachSegment = observation.position.approachSegment
        return approachSegment && approachSegment.thresholdDistanceMeters < maxThresholdDistanceMetersForApproach
      });
      // TODO Move sorting into AircraftTable.svelte?
      observationsAircraftOnApproach = sortBy(observationsAircraftOnApproachUnsorted, observation => observation.position.approachSegment!.thresholdDistanceMeters);
      observationsOtherAircraft = sortBy(observations.filter(observation => !observation.position.approachSegment), observation => observation.trajectory.aircraftProfile.callsign);
      lastTimeProcessed = time;
    });
  });
</script>

<!-- TODO What additional parameters to pass to SplitPane? See https://www.npmjs.com/package/@rich_harris/svelte-split-pane.
     TODO Work thru issue: we're using Svelte 4; svelte-split-pane expects 3.x. -->
<SplitPane
        type="vertical"
>
  <section slot="a">
    <div id="cesiumContainer"></div>
  </section>
  <section slot="b" id="tableSection">
    <!-- TODO Specify the click handler once and share below -->
    <h1>Aircraft on Approach</h1>
    <AircraftTable observations="{observationsAircraftOnApproach}" showApproachSegments={true} clickHandlerTrajectory={(trajectory) => { viewer.trackedEntity = trajectoriesToEntities.get(trajectory); }}/>
    <h1>Other Aircraft</h1>
    <AircraftTable observations="{observationsOtherAircraft}" showApproachSegments={false} clickHandlerTrajectory={(trajectory) => { viewer.trackedEntity = trajectoriesToEntities.get(trajectory); }}/>
    <div id="bottomRightBox">
      <div id="appName"><b><a href="https://github.com/dmanchester/approachminder#approachminder" target="_blank">ApproachMinder</a></b></div>
      ADS-B data by <a href="https://opensky-network.org/" target="_blank">OpenSky Network</a>
    </div>
  </section>
</SplitPane>

<style>
  #cesiumContainer {
    height: 100%;
  }

  #tableSection {
    width: auto;
    overflow: visible;
    font-family: sans-serif;
    font-size: small;
    /* Can also do padding-left, padding-right */
    margin-left: 16px;
    margin-right: 16px;
  }

  #bottomRightBox {
    position: fixed;
    bottom: 16px;
    right: 32px;
    text-align: right;
    background-color: white;  /* default background is transparent; workaround to make it opaque (and white) */
  }

  #appName {
    font-size: medium;
    margin-bottom: 8px;
  }
</style>
