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
  import type Position from '../lib/Position';
  import type Trajectory from '../lib/Trajectory';
  import type { TrajectoryCollectionTemplate } from '../lib/TrajectoryCollectionTemplate';
  import { configureViewer, createCesiumEntity, viewerOptions, type PositionWrapper } from '../lib/UI';

  import { partition } from 'lodash';

  import trajectoriesFromJSON from './data.json';
  import approachMinderConfig from '../approachminder-config.json';

  const urlParams = new URLSearchParams(window.location.search);
  const useBingImagery = urlParams.get('bing') === 'true';

  const maxThresholdDistanceMetersForApproach = 10000;
  const windowDuration = 60;  // seconds

  Ion.defaultAccessToken = approachMinderConfig.cesiumIon.accessToken;

  // Hydrate the JSON-sourced data into "real" objects.
  const trajectoryCollection = constructTrajectoryCollection(trajectoriesFromJSON as unknown as TrajectoryCollectionTemplate);

  let viewer: Viewer;
  const trajectoriesToEntities = new Map<Trajectory, Entity>();
  // Regarding initialization/population of the above variables *not* occurring here:
  //
  //   * viewer: Can only be initialized in the onMount() handler.
  //
  //   * trajectoriesToEntities: Population involves an asynchronous call--IonResource.fromAssetId()--which can't be
  //     done here.

  let initialized = $state(false);

  let time: number | null = $state(null);  // milliseconds since the epoch (see https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/Date/getTime)
  // It would be preferable for time to be a JulianDate, the type used natively by the CesiumJS viewer (and indeed,
  // through commit f796071c, this code relied on a JulianDate).
  //
  // However, using JulianDate leads to unnecessary Svelte re-rendering when the CesiumJS viewer is paused.
  //
  // Even when paused, the viewer's clock continues to emit "onTick" events. They are for the same moment in time,
  // represented by a new JulianDate instance on each tick.
  //
  // Svelte's change-detection logic compares the instances by reference equality. (It does not appear to support
  // user-defined notions of equality.) It concludes that successive instances from a paused viewer constitute a
  // difference and triggers a re-render.
  //
  // By instead using a number-based representation of time, we enable Svelte to correctly determine when the moment in
  // time has actually changed.

  let icao24ToTrack = $state(approachMinderConfig.firstICAO24ToTrack);

  let timeAsJulianDate = $derived(time ? JulianDate.fromDate(new Date(time)) : null);

  let positions = $derived(timeAsJulianDate ? trajectoryCollection.latestPositionsWithinWindow(timeAsJulianDate, windowDuration) : new Array<Position>());

  let [ posWrappersAircraftOnApproach, posWrappersOtherAircraft ] = $derived.by(() => {

    if (!timeAsJulianDate) {
      return [ new Array<PositionWrapper>(), new Array<PositionWrapper>() ];
    }

    const posWrappers: Array<PositionWrapper> = positions.map(position => ({
      position: position,
      ageSecs: JulianDate.secondsDifference(timeAsJulianDate, position.time),
    }));

    return partition(posWrappers, wrapper => {
      const approachSegment = wrapper.position.approachSegment;
      return approachSegment && approachSegment.thresholdDistanceMeters < maxThresholdDistanceMetersForApproach;
    });
  });

  $effect(() => {
    // Change the tracked entity.
    if (initialized) {  // apply this effect only once viewer has been initialized and trajectoriesToEntities has been populated
      const position = positions.find(position => position.trajectory.icao24 === icao24ToTrack);
      if (position) {
        viewer.trackedEntity = trajectoriesToEntities.get(position.trajectory);
      }  // TODO In the "else" case, should we clear viewer.trackedEntity?
    }
  });

  onMount(async () => {

    viewer = new Viewer('cesiumContainer', viewerOptions(useBingImagery));
    configureViewer(
      viewer,
      trajectoryCollection.earliestTime(),
      trajectoryCollection.latestTime(),
      trajectoryCollection.earliestTime()
    );

    const airplaneIonResource = await IonResource.fromAssetId(approachMinderConfig.cesiumIon.assetIdAirplane);

    trajectoryCollection.trajectories.forEach(trajectory => {
      const entity = createCesiumEntity(trajectory, airplaneIonResource);
      viewer.entities.add(entity);
      trajectoriesToEntities.set(trajectory, entity);
    });

    viewer.clock.onTick.addEventListener(() => {  // This gets called very frequently, even when the clock is stopped!
      time = JulianDate.toDate(viewer.clock.currentTime).getTime();
    });

    initialized = true;
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
      <AircraftTable posWrappers={posWrappersAircraftOnApproach} showApproachSegments={true} bind:icao24ToTrack={icao24ToTrack}/>
      <h1>Other Aircraft</h1>
      <AircraftTable posWrappers={posWrappersOtherAircraft} showApproachSegments={false} bind:icao24ToTrack={icao24ToTrack}/>
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
