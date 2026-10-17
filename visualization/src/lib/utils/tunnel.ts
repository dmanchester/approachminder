import {
  BoundingSphere,
  Cartesian3,
  Cartographic,
  Color,
  ColorGeometryInstanceAttribute,
  ComponentDatatype,
  EllipsoidGeodesic,
  Geometry,
  GeometryAttribute,
  GeometryAttributes,
  GeometryInstance,
  Math as CesiumMath,
  Matrix3,
  Matrix4,
  PerInstanceColorAppearance,
  Primitive,
  PrimitiveType,
  ShowGeometryInstanceAttribute,
  Transforms,
} from "cesium";

/**
 * Describes a "tunnel in the sky" for an approach to a runway: a series of elliptical cross sections ("gates"), each
 * centered on the glideslope.
 *
 * Gates are placed at thresholdDistance = firstGateDistanceMeters, firstGateDistanceMeters - gateSpacingMeters, ...,
 * down to lastGateDistanceMeters. A gate's inner radii are interpolated linearly between farGate (at
 * firstGateDistanceMeters) and nearGate (at lastGateDistanceMeters).
 */
export interface ApproachTunnelSpec {
  /** Threshold of the runway being approached. */
  thresholdLongitude: number;
  thresholdLatitude: number;
  /** Height of the threshold above the WGS84 ellipsoid (the convention of Cartesian3.fromDegrees()). */
  thresholdHeightMeters: number;
  /** Threshold at the opposite end of the runway; establishes the runway's centerline. */
  oppositeThresholdLongitude: number;
  oppositeThresholdLatitude: number;
  thresholdCrossingHeightMeters: number;
  glideslopeDegrees: number;
  firstGateDistanceMeters: number;
  lastGateDistanceMeters: number;
  gateSpacingMeters: number;
  farGate: GateRadii;
  nearGate: GateRadii;
  wallThicknessMeters: number;
  gateLengthMeters: number;
}

/** A gate's inner semi-axes. */
export interface GateRadii {
  horizontalRadiusMeters: number;
  verticalRadiusMeters: number;
}

export interface ApproachGate extends GateRadii {
  id: string;
  thresholdDistanceMeters: number;
  center: Cartesian3;
  /**
   * Transforms from the gate's local frame to Earth-fixed coordinates. In the local frame, the origin is the gate's
   * center; +x points to the right (as seen by an aircraft on the approach), +y points horizontally along the approach
   * toward the threshold, and +z points up.
   */
  frame: Matrix4;
}

// Coordinates are from analysis/src/test/scala/com/dmanchester/approachminder/data/Airports.scala.
//
// thresholdHeightMeters and thresholdCrossingHeightMeters are approximations. The threshold is ~3 m above mean sea
// level, and the geoid at SFO is ~32 m below the ellipsoid. (Trajectory altitudes, which are OpenSky's geometric
// altitudes, are passed as-is to Cartesian3.fromDegrees(); the resulting glidepath agrees with the data's on-approach
// aircraft to within a few meters.)
//
// The gates' radii are placeholders, pending radii derived from historical deviation.
export const ksfo28LTunnelSpec: ApproachTunnelSpec = {
  thresholdLongitude: -122.358349166667,
  thresholdLatitude: 37.6117119444444,
  thresholdHeightMeters: -29,
  oppositeThresholdLongitude: -122.393105444444,
  oppositeThresholdLatitude: 37.6262911111111,
  thresholdCrossingHeightMeters: 17,
  glideslopeDegrees: 3,
  firstGateDistanceMeters: 2000,
  lastGateDistanceMeters: 100,
  gateSpacingMeters: 100,
  farGate: { horizontalRadiusMeters: 45, verticalRadiusMeters: 20 },
  nearGate: { horizontalRadiusMeters: 36, verticalRadiusMeters: 16 },
  wallThicknessMeters: 1,
  gateLengthMeters: 4,
};

export function computeApproachGates(
  spec: ApproachTunnelSpec,
): Array<ApproachGate> {
  // A geodesic from the opposite threshold through the threshold; extrapolating beyond its end yields points on the
  // extended runway centerline.
  const centerline = new EllipsoidGeodesic(
    Cartographic.fromDegrees(
      spec.oppositeThresholdLongitude,
      spec.oppositeThresholdLatitude,
    ),
    Cartographic.fromDegrees(spec.thresholdLongitude, spec.thresholdLatitude),
  );
  const runwayLength = centerline.surfaceDistance;
  const tanGlideslope = Math.tan(CesiumMath.toRadians(spec.glideslopeDegrees));
  const distanceRange =
    spec.firstGateDistanceMeters - spec.lastGateDistanceMeters;

  const gates = new Array<ApproachGate>();
  for (
    let distance = spec.firstGateDistanceMeters;
    distance >= spec.lastGateDistanceMeters;
    distance -= spec.gateSpacingMeters
  ) {
    const height =
      spec.thresholdHeightMeters +
      spec.thresholdCrossingHeightMeters +
      distance * tanGlideslope;
    const center = Cartographic.toCartesian(
      withHeight(
        centerline.interpolateUsingSurfaceDistance(runwayLength + distance),
        height,
      ),
    );
    const towardThreshold = Cartographic.toCartesian(
      withHeight(
        centerline.interpolateUsingSurfaceDistance(runwayLength + distance - 1),
        height,
      ),
    );

    // Rotate the gate's local +y axis (initially north) to the approach's course.
    const enu = Transforms.eastNorthUpToFixedFrame(center);
    const local = Matrix4.multiplyByPoint(
      Matrix4.inverseTransformation(enu, new Matrix4()),
      towardThreshold,
      new Cartesian3(),
    );
    const course = Math.atan2(local.x, local.y); // clockwise from north
    const frame = Matrix4.multiplyByMatrix3(
      enu,
      Matrix3.fromRotationZ(-course),
      new Matrix4(),
    );

    const t =
      distanceRange === 0
        ? 0
        : (distance - spec.lastGateDistanceMeters) / distanceRange;
    gates.push({
      id: `gate-${distance}`,
      thresholdDistanceMeters: distance,
      center,
      frame,
      horizontalRadiusMeters: CesiumMath.lerp(
        spec.nearGate.horizontalRadiusMeters,
        spec.farGate.horizontalRadiusMeters,
        t,
      ),
      verticalRadiusMeters: CesiumMath.lerp(
        spec.nearGate.verticalRadiusMeters,
        spec.farGate.verticalRadiusMeters,
        t,
      ),
    });
  }

  return gates;
}

function withHeight(cartographic: Cartographic, height: number): Cartographic {
  cartographic.height = height;
  return cartographic;
}

/**
 * Creates geometry for a section of elliptical pipe: a closed solid consisting of outer and inner walls and annular end
 * caps. The pipe's axis is the y axis; its cross section lies in the xz plane.
 *
 * @param horizontalRadius The inner ellipse's semi-axis along x.
 * @param verticalRadius The inner ellipse's semi-axis along z.
 * @param wallThickness Added to each inner semi-axis to obtain the outer ellipse's semi-axes.
 * @param length The pipe's extent along y, centered on the origin.
 * @param segments How many segments approximate each ellipse.
 */
export function ellipticalPipeGeometry(
  horizontalRadius: number,
  verticalRadius: number,
  wallThickness: number,
  length: number,
  segments = 64,
): Geometry {
  const n = segments;
  const positions = new Float64Array(8 * n * 3);
  const normals = new Float32Array(8 * n * 3);
  const indices = new Uint16Array(8 * n * 3);

  const inner = { a: horizontalRadius, b: verticalRadius };
  const outer = {
    a: horizontalRadius + wallThickness,
    b: verticalRadius + wallThickness,
  };
  const back = -length / 2;
  const front = length / 2;

  let vertex = 0;
  const addVertex = (
    x: number,
    y: number,
    z: number,
    nx: number,
    ny: number,
    nz: number,
  ) => {
    positions.set([x, y, z], vertex * 3);
    normals.set([nx, ny, nz], vertex * 3);
    return vertex++;
  };

  let index = 0;
  const addTriangle = (i0: number, i1: number, i2: number) => {
    indices.set([i0, i1, i2], index);
    index += 3;
  };

  // Each surface gets its own vertices, so that normals are discontinuous at the pipe's edges.
  const ring = (
    ellipse: { a: number; b: number },
    y: number,
    normal: (cos: number, sin: number) => [number, number, number],
  ) => {
    const first = vertex;
    for (let i = 0; i < n; i++) {
      const theta = (2 * Math.PI * i) / n;
      const cos = Math.cos(theta);
      const sin = Math.sin(theta);
      addVertex(ellipse.a * cos, y, ellipse.b * sin, ...normal(cos, sin));
    }
    return (i: number) => first + (i % n);
  };

  // The normal of the ellipse (a cos θ, b sin θ) is along (b cos θ, a sin θ).
  const radial =
    (ellipse: { a: number; b: number }, sign: number) =>
    (cos: number, sin: number): [number, number, number] => {
      const nx = ellipse.b * cos;
      const nz = ellipse.a * sin;
      const magnitude = Math.hypot(nx, nz);
      return [(sign * nx) / magnitude, 0, (sign * nz) / magnitude];
    };
  const axial = (sign: number) => (): [number, number, number] => [0, sign, 0];

  const outerBack = ring(outer, back, radial(outer, 1));
  const outerFront = ring(outer, front, radial(outer, 1));
  const innerBack = ring(inner, back, radial(inner, -1));
  const innerFront = ring(inner, front, radial(inner, -1));
  const backCapOuter = ring(outer, back, axial(-1));
  const backCapInner = ring(inner, back, axial(-1));
  const frontCapOuter = ring(outer, front, axial(1));
  const frontCapInner = ring(inner, front, axial(1));

  // Triangles wind counterclockwise as seen from outside the solid.
  for (let i = 0; i < n; i++) {
    const j = i + 1;
    addTriangle(outerBack(i), outerFront(j), outerBack(j));
    addTriangle(outerBack(i), outerFront(i), outerFront(j));
    addTriangle(innerBack(i), innerBack(j), innerFront(j));
    addTriangle(innerBack(i), innerFront(j), innerFront(i));
    addTriangle(backCapOuter(i), backCapOuter(j), backCapInner(j));
    addTriangle(backCapOuter(i), backCapInner(j), backCapInner(i));
    addTriangle(frontCapOuter(i), frontCapInner(j), frontCapOuter(j));
    addTriangle(frontCapOuter(i), frontCapInner(i), frontCapInner(j));
  }

  const attributes = new GeometryAttributes();
  attributes.position = new GeometryAttribute({
    componentDatatype: ComponentDatatype.DOUBLE,
    componentsPerAttribute: 3,
    values: positions,
  });
  attributes.normal = new GeometryAttribute({
    componentDatatype: ComponentDatatype.FLOAT,
    componentsPerAttribute: 3,
    values: normals,
  });

  return new Geometry({
    attributes,
    indices,
    primitiveType: PrimitiveType.TRIANGLES,
    boundingSphere: BoundingSphere.fromVertices(Array.from(positions)),
  });
}

/**
 * Creates a single Primitive containing all of the gates. Show/hide the whole tunnel via the Primitive's "show"
 * property, or individual gates via setGateShown().
 */
export function createApproachTunnelPrimitive(
  gates: Array<ApproachGate>,
  wallThicknessMeters: number,
  gateLengthMeters: number,
  color: Color,
): Primitive {
  return new Primitive({
    geometryInstances: gates.map(
      (gate) =>
        new GeometryInstance({
          id: gate.id,
          geometry: ellipticalPipeGeometry(
            gate.horizontalRadiusMeters,
            gate.verticalRadiusMeters,
            wallThicknessMeters,
            gateLengthMeters,
          ),
          modelMatrix: gate.frame,
          attributes: {
            color: ColorGeometryInstanceAttribute.fromColor(color),
            show: new ShowGeometryInstanceAttribute(true),
          },
        }),
    ),
    appearance: new PerInstanceColorAppearance({
      translucent: color.alpha < 1,
      closed: true,
    }),
    // Required for geometry that isn't created by a web worker.
    asynchronous: false,
  });
}

/**
 * Shows or hides an individual gate. Only effective once the primitive has been rendered at least once.
 */
export function setGateShown(
  primitive: Primitive,
  gateId: string,
  shown: boolean,
): void {
  const attributes = primitive.getGeometryInstanceAttributes(gateId);
  if (attributes) {
    attributes.show = ShowGeometryInstanceAttribute.toValue(shown);
  }
}
