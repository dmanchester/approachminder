import type Position from "./Position";

export type Observation = ({
  position: Position,
  ageOfObservation: number,
  trackEntityFunc: () => void
});