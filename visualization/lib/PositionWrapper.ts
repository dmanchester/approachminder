import { type Position } from "./Position";

/**
 * Wraps a Position and adds a UI-oriented field: the age of the position (relative to a current point in time).
 */
export type PositionWrapper = ({
  position: Position,
  ageSecs: number
});
