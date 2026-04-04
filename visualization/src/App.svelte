<script lang="ts">
  import { onMount } from 'svelte';
  import { SplitPane } from '@rich_harris/svelte-split-pane';
  import {
    Entity,
    Ion,
    IonResource,
    JulianDate,
    Viewer
  } from 'cesium';
  import 'cesium/Build/Cesium/Widgets/widgets.css';

  import AircraftTable from './AircraftTable.svelte';
  import { constructTrajectoryCollection } from '../lib/IO';
  import type { Observation } from '../lib/Observation';
  import type Trajectory from '../lib/Trajectory';
  import type { TrajectoryCollectionTemplate } from "../lib/TrajectoryCollectionTemplate";
  import { configureViewer, createCesiumEntities, viewerOptions } from "../lib/UI";

  import { partition } from 'lodash';

  import trajectoriesFromJSON from './data.json';
  import approachMinderConfig from '../approachminder-config.json';

  const urlParams = new URLSearchParams(window.location.search);
  const useBingImagery = urlParams.get('bing') === 'true';

  const maxThresholdDistanceMetersForApproach = 10000;
  const windowDuration = 60;  // seconds
  const firstCallsignToTrack = 'SKW4081';

  Ion.defaultAccessToken = approachMinderConfig.cesiumIon.accessToken;

  const trajectoryCollection = constructTrajectoryCollection(trajectoriesFromJSON as unknown as TrajectoryCollectionTemplate);
  const firstTrajectoryToTrack = trajectoryCollection.trajectories.find(trajectory => trajectory.aircraftProfile.callsign === firstCallsignToTrack)!;

  let time: JulianDate | null = $state(null);

  let [ observationsAircraftOnApproach, observationsOtherAircraft ] = $derived.by(() => {

    let observationsAircraftOnApproach: Array<Observation>;
    let observationsOtherAircraft: Array<Observation>;

    if (time === null) {
      observationsAircraftOnApproach = [];
      observationsOtherAircraft = [];
    } else {

      // Get the latest positions within the time window, one per aircraft.
      const latestPositionsWithinWindow = trajectoryCollection.latestPositionsWithinWindow(time, windowDuration);

      const observations = latestPositionsWithinWindow.map(position => ({
        position: position,
        ageOfObservation: Math.round(JulianDate.secondsDifference(time!, position.time))
      }));

      [ observationsAircraftOnApproach, observationsOtherAircraft ] = partition(observations, observation => {
        const approachSegment = observation.position.approachSegment;
        return approachSegment && approachSegment.thresholdDistanceMeters < maxThresholdDistanceMetersForApproach;
      });
    }

    return [ observationsAircraftOnApproach, observationsOtherAircraft ];
  });

  // For "trajectoriesToEntities" and "viewer" to be visible to this file's UI template, they must have top-level
  // declarations like the following. However, they can't actually be initialized here. ("trajectoriesToEntities"
  // indirectly requires asynchronous initialization--see IonResource.fromAssetId()--which can't be done here.
  // Meanwhile, "viewer" can only be initialized in the onMount() handler.)
  let trajectoriesToEntities: Map<Trajectory, Entity>;
  let viewer: Viewer;

  onMount(async () => {

    viewer = new Viewer('cesiumContainer', viewerOptions(useBingImagery));
    configureViewer(
      viewer,
      trajectoryCollection.earliestTime(),
      trajectoryCollection.latestTime(),
      firstTrajectoryToTrack.earliestTime()
    );

    const airplaneIonResource = await IonResource.fromAssetId(approachMinderConfig.cesiumIon.assetIdAirplane);
    trajectoriesToEntities = createCesiumEntities(trajectoryCollection.trajectories, airplaneIonResource);
    trajectoriesToEntities.values().forEach(entity => { viewer.entities.add(entity); });
    viewer.trackedEntity = trajectoriesToEntities.get(firstTrajectoryToTrack);

    viewer.clock.onTick.addEventListener(() => {  // This gets called very frequently, even when the clock is stopped!
      time = viewer.clock.currentTime;
    });
  });
</script>

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
