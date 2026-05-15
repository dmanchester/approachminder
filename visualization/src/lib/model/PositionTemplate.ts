import { type ApproachSegment } from "./ApproachSegment";

/**
 * The PositionTemplate type facilitates the creation of Position instances.
 *
 * Position instances are generally created by the Trajectory constructor; specifically, when the constructor is
 * creating the Trajectory instance to which the Position instances will belong.
 *
 * This type provides the mechanism by which client code can supply information to the Trajectory constructor for
 * constructing Position instances.
 *
 * This type includes all Position fields *except:*
 *
 *   * trajectory: The Trajectory constructor provides this information.
 *
 *   * time: The Trajectory constructor receives PositionTemplate via a time-keyed Object/Record. (So, it is trivial to
 *           match a PositionTemplate with the time to which it applies.)
 */
export type PositionTemplate = {
  longitude: number,
  latitude: number,
  altitude: number,
  onGround: boolean,
  velocity: number | null,
  trueTrack: number | null,
  verticalRate: number | null,
  squawk: string | null,
  approachSegment: ApproachSegment | null,
}