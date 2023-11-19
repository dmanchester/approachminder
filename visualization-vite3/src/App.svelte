<script>
  import { onMount } from "svelte";
  import { Cartesian3, Ion, IonResource, JulianDate, SampledPositionProperty, Terrain, VelocityOrientationProperty, Viewer, createWorldTerrainAsync } from "cesium";
  import IO from "../lib/IO.js";
  import "../node_modules/cesium/Source/Widgets/widgets.css";  // TODO Compare with 'import "cesium/Build/Cesium/Widgets/widgets.css"'; and, what do I get from this?
  import trajectoriesFromJSON from "./data.json";

  window['CESIUM_BASE_URL'] = '/libs/cesium'

  // TODO Externalize this.
  Ion.defaultAccessToken = 'eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJqdGkiOiI2MzQ3MmQ0ZC1mNWU1LTQ2YzItYTRjMS01NGIxYzRjMGIwZTUiLCJpZCI6MTMyMTg5LCJpYXQiOjE2ODA2NTY4ODN9.CSgIJqm0gEDGCXXvbuW932tn04Q1m8Y_AmssiRXgR8Y';

  let aircraftSeenRecently;  // TODO Initialize to zero?
  let viewer;
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

      viewer.clock.onTick.addEventListener(() => {   // Whoa, this gets called all the time, even when clock is stopped!

          const time = viewer.clock.currentTime;

          if (JulianDate.equals(time, lastTimeProcessed)) {
              // Nothing to do.
              return;
          }

          const aircraftLatestPositionsWithinWindow = trajectories.aircraftLatestPositionsWithinWindow(time, windowDuration);

          aircraftSeenRecently = aircraftLatestPositionsWithinWindow.length;
          // let message = `As of ${JulianDate.toIso8601(time)}, ${aircraftLatestPositionsWithinWindow.length} aircraft seen in last ${windowDuration} seconds:\n`;
          // aircraftLatestPositionsWithinWindow.forEach(([aircraftProfile, tbp]) => { message += `  ${aircraftProfile.icao24} ${JulianDate.toIso8601(tbp.time)}\n` });
          // console.log(message);

          lastTimeProcessed = time;
      });

      viewer.clock.shouldAnimate = true;

      const airplaneUri = await IonResource.fromAssetId(1621363);

      let trackedEntitySet = false;  // FIXME This is a big hack
      trajectories.theTrajectories.forEach(trajectory => {

          const times = trajectory.timeBasedPositions.map(timeBasedPosition => timeBasedPosition.time);
          const positions = trajectory.timeBasedPositions.map(timeBasedPosition => timeBasedPosition.position);

          const positionProperty = new SampledPositionProperty();
          positionProperty.addSamples(times, positions);

          const airplaneEntity = viewer.entities.add({
              name: trajectory.aircraftProfile.icao24,
              //  availability: new Cesium.TimeIntervalCollection([ new Cesium.TimeInterval({ start: start, stop: stop }) ]),
              position: positionProperty,
              model: { uri: airplaneUri },
              // Automatically compute the orientation from the position.
              orientation: new VelocityOrientationProperty(positionProperty),
          });

          if (!trackedEntitySet) {
              viewer.trackedEntity = airplaneEntity;
              viewer.clock.currentTime = trajectory.earliestTime().clone();
              trackedEntitySet = true;
          }
      });
  });
</script>

<div id="cesiumContainer"></div>
<div id="toolbar" style="margin: 5px; padding: 2px 5px; position: absolute; color: #eee; top: 0; left: 0">
    <table>
        <tbody>
        <tr>
            <td>This is column 1</td>
            <td>{aircraftSeenRecently} aircraft seen recently</td>
        </tr>
        </tbody>
    </table>
</div>
