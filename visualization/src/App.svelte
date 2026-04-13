<script lang="ts">
  import { onMount } from 'svelte';
  import { SplitPane } from '@rich_harris/svelte-split-pane';
  import {
    Ion,
    IonResource,
    JulianDate,
    Viewer
  } from 'cesium';
  import 'cesium/Build/Cesium/Widgets/widgets.css';

  import AircraftTable from './AircraftTable.svelte';
  import { constructTrajectoryCollection } from '../lib/IO';
  import type Trajectory from '../lib/Trajectory';
  import type { TrajectoryCollectionTemplate } from "../lib/TrajectoryCollectionTemplate";
  import { configureViewer, createCesiumEntity, viewerOptions, type PositionWrapper } from "../lib/UI";

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
  const firstTrajectoryToTrack = trajectoryCollection.trajectories.find(trajectory => trajectory.callsign === firstCallsignToTrack)!;

  // For "viewer" and "trajectoriesToTrackEntityFuncs" to be visible to this file's UI template, they must have
  // top-level declarations like the following. However, they can't actually be initialized ("viewer")/populated
  // ("trajectoriesToTrackEntityFuncs") here. ("viewer" can only be initialized in the onMount() handler. Meanwhile,
  // population of "trajectoriesToTrackEntityFuncs" involves an asynchronous call--IonResource.fromAssetId()--which
  // can't be done here.)
  let viewer: Viewer;
  // When called, a trajectory's "track-entity function" leads the viewer to begin tracking the entity (3-D model) that
  // flies that trajectory.
  const trajectoriesToTrackEntityFuncs = new Map<Trajectory, () => void>();

  let time: number | null = $state(null);  // milliseconds since the epoch (see https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/Date/getTime)
  // It would be preferable for time to be a JulianDate, the type used natively by the CesiumJS viewer (and indeed,
  // through commit f796071c, this code relied on a JulianDate).
  //
  // However, using JulianDate leads to unnecessary Svelte re-rendering when the CesiumJS viewer is paused.
  //
  // Even when paused, the viewer's clock continues to emit "onTick" events. They are for the same moment in time,
  // represented by a new JulianDate instance on each tick.
  //
  // Svelte's change-detection logic compares the instances by reference equality. (It appears not to support
  // user-defined notions of equality.) It concludes that successive instances from a paused viewer constitute a
  // difference and triggers a re-render.
  //
  // By instead using a number-based representation of time, we enable Svelte to correctly determine when the moment in
  // time has actually changed.

  onMount(async () => {

    viewer = new Viewer('cesiumContainer', viewerOptions(useBingImagery));
    configureViewer(
      viewer,
      trajectoryCollection.earliestTime(),
      trajectoryCollection.latestTime(),
      firstTrajectoryToTrack.earliestTime()
    );

    const airplaneIonResource = await IonResource.fromAssetId(approachMinderConfig.cesiumIon.assetIdAirplane);

    trajectoryCollection.trajectories.forEach(trajectory => {
      const entity = createCesiumEntity(trajectory, airplaneIonResource);
      viewer.entities.add(entity);
      const trackEntityFunc = () => { viewer.trackedEntity = entity; };
      trajectoriesToTrackEntityFuncs.set(trajectory, trackEntityFunc);
    });

    // Begin tracking the entity of the designated trajectory (by calling the trajectory's function).
    trajectoriesToTrackEntityFuncs.get(firstTrajectoryToTrack)!();

    viewer.clock.onTick.addEventListener(() => {  // This gets called very frequently, even when the clock is stopped!
      time = JulianDate.toDate(viewer.clock.currentTime).getTime();
    });
  });

  let [ posWrappersAircraftOnApproach, posWrappersOtherAircraft ] = $derived.by(() => {

    if (time === null) {
      return [ new Array<PositionWrapper>(), new Array<PositionWrapper>() ];
    }

    const timeAsJulianDate = JulianDate.fromDate(new Date(time));

    // Get the latest positions within the time window, one per aircraft.
    const latestPositionsWithinWindow = trajectoryCollection.latestPositionsWithinWindow(timeAsJulianDate, windowDuration);

    const posWrappers: Array<PositionWrapper> = latestPositionsWithinWindow.map(position => ({
      position: position,
      ageSecs: Math.round(JulianDate.secondsDifference(timeAsJulianDate!, position.time)),
      trackEntityFunc: trajectoriesToTrackEntityFuncs.get(position.trajectory)!
    }));

    return partition(posWrappers, wrapper => {
      const approachSegment = wrapper.position.approachSegment;
      return approachSegment && approachSegment.thresholdDistanceMeters < maxThresholdDistanceMetersForApproach;
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
      <h1>Aircraft on Approach</h1>
      <AircraftTable posWrappers={posWrappersAircraftOnApproach} showApproachSegments={true}/>
      <h1>Other Aircraft</h1>
      <AircraftTable posWrappers={posWrappersOtherAircraft} showApproachSegments={false}/>
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
