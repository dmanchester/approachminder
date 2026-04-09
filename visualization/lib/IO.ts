import ApproachSegment from "./ApproachSegment";
import type { PositionTemplate } from "./PositionTemplate";
import Trajectory from "./Trajectory";
import TrajectoryCollection from "./TrajectoryCollection";
import type { TrajectoryCollectionTemplate } from "./TrajectoryCollectionTemplate";

/**
 * Construct a TrajectoryCollection instance.
 *
 * @param template The data to use in constructing the instance. Typically, a parse/import of file
 * "visualization/src/data.json".
 * @throws {Error} If template is empty (i.e., it contains no trajectories); or, if a trajectory in the template
 *                 contains no positions.
 */
export function constructTrajectoryCollection(template: TrajectoryCollectionTemplate) {

  const trajectories = template.map(trajectoryTemplatePart => {

    const positionTemplates: Array<[string, PositionTemplate]> = Object.entries(trajectoryTemplatePart.positions).map(([time, positionTemplatePart]) => {

      const approachSegmentTemplatePart = positionTemplatePart.approachSegment;
      const approachSegment = approachSegmentTemplatePart ?
        new ApproachSegment(approachSegmentTemplatePart.airport, approachSegmentTemplatePart.threshold, approachSegmentTemplatePart.thresholdDistanceMeters, approachSegmentTemplatePart.verticalDevMeters, approachSegmentTemplatePart.horizontalDevMeters, approachSegmentTemplatePart.normalizedEuclideanDistance) :
        null;

      return [
        time,
        {  // a PositionTemplate
          longitude: positionTemplatePart.longitude,
          latitude: positionTemplatePart.latitude,
          altitude: positionTemplatePart.altitude,
          onGround: positionTemplatePart.onGround,
          velocity: positionTemplatePart.velocity,
          trueTrack: positionTemplatePart.trueTrack,
          verticalRate: positionTemplatePart.verticalRate,
          squawk: positionTemplatePart.squawk,
          approachSegment,
        }
      ];
    });

    return new Trajectory(trajectoryTemplatePart.icao24, trajectoryTemplatePart.callsign, trajectoryTemplatePart.category, Object.fromEntries(positionTemplates));
  });

  return new TrajectoryCollection(trajectories);
}
