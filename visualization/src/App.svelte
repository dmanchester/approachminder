<script lang="ts">
  import { onMount } from 'svelte';
  import { SplitPane } from '@rich_harris/svelte-split-pane';
  import AircraftTable from './AircraftTable.svelte';
  import {
    Entity,
    Ion,
    IonResource,
    JulianDate,
    Viewer
  } from 'cesium';
  import '../node_modules/cesium/Source/Widgets/widgets.css';  // TODO Compare with "import 'cesium/Build/Cesium/Widgets/widgets.css'"; and, what do I get from this?
  import { sortBy } from 'lodash';

  import IO from '../lib/IO';
  import type { Observation } from '../lib/Observation';
  import type Trajectory from '../lib/Trajectory';
  import trajectoriesFromJSON from './data.json';
  import approachMinderConfig from '../approachminder-config.json';
  import { configureViewer, createCesiumEntities, viewerOptions } from "../lib/UI";

  Ion.defaultAccessToken = approachMinderConfig.cesiumIon.accessToken;

  const start = JulianDate.fromIso8601('2022-12-06T18:49:09Z');
  const stop = JulianDate.fromIso8601('2022-12-06T18:56:01Z');
  const maxThresholdDistanceMetersForApproach = 10000;
  const windowDuration = 60;  // seconds
  const firstCallsignToTrack = 'SKW4081';

  const urlParams = new URLSearchParams(window.location.search);
  const useBingImagery = urlParams.get('bing') === 'true';

  const trajectories = IO.trajectoriesFromParsedJSON(trajectoriesFromJSON as any);
  let trajectoriesToEntities: Map<Trajectory, Entity>;

  // While we have to defer initialization of the viewer until the onMount() handler has fired, we seemingly must make
  // it a top-level declaration so it's visible to the trajectory click handlers in this file's UI template.
  let viewer: Viewer;
  let observationsAircraftOnApproach: Array<Observation> = $state([]);
  let observationsOtherAircraft: Array<Observation> = $state([]);

  onMount(async () => {

    // TODO What code that's currently in this function can be moved before it, to improve startup performance?

    viewer = new Viewer('cesiumContainer', viewerOptions(useBingImagery));
    configureViewer(viewer, start, stop);

    const airplaneIonResource = await IonResource.fromAssetId(approachMinderConfig.cesiumIon.assetIdAirplane);
    trajectoriesToEntities = createCesiumEntities(trajectories.theTrajectories, airplaneIonResource);
    trajectoriesToEntities.values().forEach(entity => { viewer.entities.add(entity); });

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

<!-- TODO What additional parameters to pass to SplitPane? See https://www.npmjs.com/package/@rich_harris/svelte-split-pane. -->
<SplitPane type="rows">
  {#snippet a()}
    <section>
      <div id="cesiumContainer"></div>
    </section>
  {/snippet}
  {#snippet b()}
    <section id="tableSection">
      <!-- TODO Specify the click handler once and share below -->
      <h1>Aircraft on Approach</h1>
      <AircraftTable observations={observationsAircraftOnApproach} showApproachSegments={true} clickHandlerTrajectory={(trajectory) => { viewer.trackedEntity = trajectoriesToEntities.get(trajectory); }}/>
      <h1>Other Aircraft</h1>
      <AircraftTable observations={observationsOtherAircraft} showApproachSegments={false} clickHandlerTrajectory={(trajectory) => { viewer.trackedEntity = trajectoriesToEntities.get(trajectory); }}/>
      <div id="bottomRightBox">
        <div id="appName"><b><a href="https://github.com/dmanchester/approachminder#approachminder" target="_blank">ApproachMinder</a></b></div>
        ADS-B data by <a href="https://opensky-network.org/" target="_blank">OpenSky Network</a>
      </div>
    </section>
  {/snippet}
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
