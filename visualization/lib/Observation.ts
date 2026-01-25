import type TimeBasedPosition from "./TimeBasedPosition";
import type Trajectory from "./Trajectory";

export type Observation = ({
  trajectory: Trajectory,
  position: TimeBasedPosition,
  ageOfObservation: number
})