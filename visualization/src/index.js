import { Cartesian3, Ion, IonResource, JulianDate, SampledPositionProperty, Terrain, VelocityOrientationProperty, Viewer } from "cesium";
import IO from "../lib/IO.mjs";
import "cesium/Build/Cesium/Widgets/widgets.css";  // TODO What do I get from this?
import "../src/css/main.css";  // TODO Vet contents of this.
import trajectoriesFromJSON from "./data.json";

// TODO Externalize this.
Ion.defaultAccessToken = 'eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJqdGkiOiI2MzQ3MmQ0ZC1mNWU1LTQ2YzItYTRjMS01NGIxYzRjMGIwZTUiLCJpZCI6MTMyMTg5LCJpYXQiOjE2ODA2NTY4ODN9.CSgIJqm0gEDGCXXvbuW932tn04Q1m8Y_AmssiRXgR8Y';

const viewer = new Viewer('cesiumContainer', {
  terrain: Terrain.fromWorldTerrain(),   // TODO Compare to "terrainProvider: await createWorldTerrainAsync()"
});

const trajectories = IO.trajectoriesFromParsedJSON(trajectoriesFromJSON);

/* Initialize the viewer clock:
  Initialize the viewer's clock by setting its start and stop to the flight start and stop times we just calculated.
  Also, set the viewer's current time to the start time and take the user to that time.
*/
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

  let message = `As of ${JulianDate.toIso8601(time)}, ${aircraftLatestPositionsWithinWindow.length} aircraft seen in last ${windowDuration} seconds:\n`;
  aircraftLatestPositionsWithinWindow.forEach(([aircraftProfile, tbp]) => { message += `  ${aircraftProfile.icao24} ${JulianDate.toIso8601(tbp.time)}\n` });
  console.log(message);

  lastTimeProcessed = time;
});

viewer.clock.shouldAnimate = true;

async function doIt() {

  const airplaneUri = await IonResource.fromAssetId(1621363);

  let trackedEntitySet = false;  // FIXME This is a big hack
  trajectories.theTrajectories.forEach((trajectory) => {

    const times = trajectory.timeBasedPositions.map((timeBasedPosition) => timeBasedPosition.time);
    const positions = trajectory.timeBasedPositions.map((timeBasedPosition) => timeBasedPosition.position);

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
}

doIt();