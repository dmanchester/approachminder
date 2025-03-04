package com.dmanchester.approachminder

/**
 * As a tree-like structure, models a set of trajectories that approach a reference point. For example:
 *
 *          ref point
 *   100 m      *
 *   200 m      *
 *   300 m      *
 *   400 m      *
 *   500 m    * *
 *   600 m   *   *
 *   700 m   *    *
 *   800 m   *    * *
 *   900 m   *    *  *
 *  1000 m   *        *
 *  1100 m   *    *    *
 *  1200 m   *    *    *
 *  1300 m   *    *     *
 *  1400 m        *      *
 *  1500 m        *      *
 *
 * A TrajectoryTree is typically derived from a large number of source trajectories. Based on those trajectories, at
 * fixed distances from the reference point, the derivation process calculates a smaller number of TrajectorySegments
 * (the "*" in the above example) via a clustering algorithm. Each segment consists of a polar angle, an altitude, and
 * related statistics from the derivation.
 *
 * The segments are the extent of the data that the TrajectoryTree maintains. In particular, the tree makes no attempt
 * to "connect" segments at different distances into trajectories.
 *
 * The number of segments at each distance is generally expected to trend downward with decreasing distance, reaching
 * one segment at the smallest distances. Whether this trend is realized for a given tree, though, is dependent on the
 * source trajectories, and on the other parameters passed during tree construction.
 *
 * The number of segments may also vary with data availability, and with nuances in the clustering output. The
 * above example having three segments at 1300 m but only two segments at 1500 m suggests some source trajectories began
 * around 1300 m.
 *
 * The example's "gap" at 1000 m--two segments at that distance, but three at 1100 m and 900 m--suggests a nuance in the
 * clustering output.
 */
class TrajectoryTree private(val segments: Map[BigDecimal, Seq[AngleAndAltitudeWithStats]]) {

}
