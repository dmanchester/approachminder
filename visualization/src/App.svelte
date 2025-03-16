<script>
  import { onMount } from "svelte";
  import { SplitPane } from '@rich_harris/svelte-split-pane';
  import AircraftTable from './AircraftTable.svelte';
  import {
    Cartesian3,
    createWorldTerrainAsync,
    Entity,
    ImageryLayer,
    Ion,
    IonResource,
    IonImageryProvider,
    JulianDate,
    SampledPositionProperty,
    VelocityOrientationProperty,
    Viewer
  } from "cesium";
  import sortBy from "lodash/sortBy.js";
  import IO from "../lib/IO.js";
  import "../node_modules/cesium/Source/Widgets/widgets.css";  // TODO Compare with 'import "cesium/Build/Cesium/Widgets/widgets.css"'; and, what do I get from this?
  import trajectoriesFromJSON from "./data.json";

  window['CESIUM_BASE_URL'] = '/libs/cesium'

  // TODO Externalize access token.
  Ion.defaultAccessToken = '*** INSERT ACCESS TOKEN FROM https://ion.cesium.com/ ***';
  const useBingImagery = false;
  const maxThresholdDistanceMetersForApproach = 10000;

  let viewer;
  const trajectoriesToEntities = new Map();
  let observationsAircraftOnApproach = [];
  let observationsOtherAircraft = [];

  onMount(async () => {
      try {
          const viewerOptions = {
            baseLayerPicker: false,
            geocoder: false,
            homeButton: false,
            sceneModePicker: false,
            terrainProvider: await createWorldTerrainAsync()  // TODO Compare to: "terrain: Terrain.fromWorldTerrain()"
          };

          if (!useBingImagery) {
            // Use Sentinel-2 imagery. See:
            //
            //   * https://sandcastle.cesium.com/?src=Sentinel-2.html
            //   * https://cesium.com/learn/ion/optimizing-quotas/
            viewerOptions.baseLayer = ImageryLayer.fromProviderAsync(IonImageryProvider.fromAssetId(3954));
          }

          viewer = new Viewer('cesiumContainer', viewerOptions);

      } catch(error) {
          console.log(error);
      }

      const trajectories = IO.trajectoriesFromParsedJSON(trajectoriesFromJSON);

      const start = trajectories.earliestTime();
      const stop = trajectories.latestTime();
      viewer.clock.startTime = start.clone();
      viewer.clock.stopTime = stop.clone();
      viewer.timeline.zoomTo(start, stop);
// Speed up the playback speed 5x.
//    viewer.clock.multiplier = 5;
// Start playing the scene.

      const windowDuration = 60;  // seconds
      let lastTimeProcessed = undefined;

      viewer.clock.shouldAnimate = true;

      const airplaneUri = await IonResource.fromAssetId(3164521);  // "B737-800 Model"

      trajectories.theTrajectories.forEach(trajectory => {

          const times = trajectory.timeBasedPositions.map(timeBasedPosition => timeBasedPosition.time);
          const positions = trajectory.timeBasedPositions.map(timeBasedPosition => Cartesian3.fromDegrees(timeBasedPosition.longitude, timeBasedPosition.latitude, timeBasedPosition.altitude));

          const positionProperty = new SampledPositionProperty();
          positionProperty.addSamples(times, positions);

          const entity = new Entity({
              name: trajectory.aircraftProfile.icao24,
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

      // TODO This is hacky. Also, concern ourselves with no-entities case?
      const firstTrajectory = trajectories.theTrajectories[0];
      viewer.trackedEntity = trajectoriesToEntities.get(firstTrajectory);
      viewer.clock.currentTime = firstTrajectory.earliestTime().clone();

      viewer.clock.onTick.addEventListener(() => {   // Whoa, this gets called all the time, even when clock is stopped!

        const time = viewer.clock.currentTime;

        if (JulianDate.equals(time, lastTimeProcessed)) {
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
        const observationsAircraftOnApproachUnsorted = observations.filter(observation => observation.position.approachSegment?.thresholdDistanceMeters < maxThresholdDistanceMetersForApproach);
        // TODO Move sorting into AircraftTable.svelte?
        observationsAircraftOnApproach = sortBy(observationsAircraftOnApproachUnsorted, observation => observation.position.approachSegment.thresholdDistanceMeters);
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
  }

  #appName {
    font-size: medium;
    margin-bottom: 8px;
  }
</style>
