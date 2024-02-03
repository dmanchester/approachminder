<script>
  import { onMount } from "svelte";
  import { SplitPane } from '@rich_harris/svelte-split-pane';
  import {
      Cartesian3,
      Ion,
      IonResource,
      JulianDate,
      SampledPositionProperty,
      Terrain,
      VelocityOrientationProperty,
      Viewer,
      createWorldTerrainAsync,
      Entity
  } from "cesium";
  import sortBy from "lodash/sortBy.js";
  import IO from "../lib/IO.js";
  import "../node_modules/cesium/Source/Widgets/widgets.css";  // TODO Compare with 'import "cesium/Build/Cesium/Widgets/widgets.css"'; and, what do I get from this?
  import trajectoriesFromJSON from "./data.json";

  window['CESIUM_BASE_URL'] = '/libs/cesium'

  // TODO Externalize this.
  Ion.defaultAccessToken = 'eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJqdGkiOiI2MzQ3MmQ0ZC1mNWU1LTQ2YzItYTRjMS01NGIxYzRjMGIwZTUiLCJpZCI6MTMyMTg5LCJpYXQiOjE2ODA2NTY4ODN9.CSgIJqm0gEDGCXXvbuW932tn04Q1m8Y_AmssiRXgR8Y';

  let viewer;
  const trajectoriesToEntities = new Map();
  let observations = [];

  onMount(async () => {
      try {
          viewer = new Viewer('cesiumContainer', {
              terrainProvider: await createWorldTerrainAsync()  // TODO Compare to: "terrain: Terrain.fromWorldTerrain()"
          });
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

      const airplaneUri = await IonResource.fromAssetId(1621363);

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
        const latestPositionsWithinWindow = trajectories.latestPositionsWithinWindow(time, windowDuration);
        // TODO First time I've used the "observations" terminology. If it sticks, broaden back to the Scala code?
        const observationsUnsorted = latestPositionsWithinWindow.map(([trajectory, timeBasedPosition]) => ({
          entity: trajectoriesToEntities.get(trajectory),
          icao24: trajectory.aircraftProfile.icao24,
          longitude: timeBasedPosition.longitude,
          latitude: timeBasedPosition.latitude,
          altitude: timeBasedPosition.altitude,
          ageOfObservation: Math.round(JulianDate.secondsDifference(time, timeBasedPosition.time))
        }));
        observations = sortBy(observationsUnsorted, observation => observation.icao24);

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
  <section slot="b">
    <table id="aircraftTable">
      <tbody>
      {#each observations as observation (observation.icao24)}
        <tr>
          <td>
            <button on:click={() => { viewer.trackedEntity = observation.entity; }}>
              {observation.icao24}
            </button>
          </td>
          <td>
            {observation.longitude}
          </td>
          <td>
            {observation.latitude}
          </td>
          <td>
            {observation.altitude}
            <!-- TODO Need to add in some factor to address "height above ellipsoid" vs. "height above geoid", get to a plausible height above MSL -->
          </td>
          <td>
            {observation.ageOfObservation}
          </td>
        </tr>
      {/each}
      </tbody>
    </table>
  </section>
</SplitPane>

<style>
  #cesiumContainer {
    width: 100%;
    height: 100%;
    margin: 0;
    padding: 0;
    overflow: hidden;
  }
  #aircraftTable {
    /* TODO This is the default. How to prevent "overflow: hidden" trickle-down (not from #cesiumContainer; from
         elsewhere) such that we obviate the need for this? */
    overflow: visible;
  }
</style>
