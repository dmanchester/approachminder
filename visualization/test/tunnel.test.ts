import {
  computeApproachGates,
  ellipticalPipeGeometry,
  ksfo28LTunnelSpec,
} from "../src/lib/utils/tunnel";

import { Cartesian3, Cartographic, Math as CesiumMath, Matrix4 } from "cesium";
import { describe, expect, test } from "vitest";

describe("ellipticalPipeGeometry()", () => {
  const segments = 16;
  const geometry = ellipticalPipeGeometry(10, 5, 1, 4, segments);
  const positions = geometry.attributes.position!.values as Float64Array;
  const normals = geometry.attributes.normal!.values as Float32Array;
  const indices = geometry.indices!;
  const vertex = (i: number) =>
    Cartesian3.fromArray(Array.from(positions), i * 3);
  const normal = (i: number) =>
    Cartesian3.fromArray(Array.from(normals), i * 3);

  test("should have walls and end caps", () => {
    expect(positions.length / 3).toBe(8 * segments);
    expect(indices.length / 3).toBe(8 * segments);
  });

  test("should lie between the inner and outer ellipses, within the length", () => {
    for (let i = 0; i < positions.length / 3; i++) {
      const p = vertex(i);
      const innerValue = (p.x / 10) ** 2 + (p.z / 5) ** 2;
      const outerValue = (p.x / 11) ** 2 + (p.z / 6) ** 2;
      expect(innerValue).toBeGreaterThanOrEqual(1 - 1e-9);
      expect(outerValue).toBeLessThanOrEqual(1 + 1e-9);
      expect(Math.abs(p.y)).toBeCloseTo(2);
    }
  });

  test("should wind every triangle counterclockwise as seen from outside", () => {
    for (let t = 0; t < indices.length; t += 3) {
      const [i0, i1, i2] = [indices[t], indices[t + 1], indices[t + 2]];
      const p0 = vertex(i0);
      const faceNormal = Cartesian3.cross(
        Cartesian3.subtract(vertex(i1), p0, new Cartesian3()),
        Cartesian3.subtract(vertex(i2), p0, new Cartesian3()),
        new Cartesian3(),
      );
      expect(Cartesian3.dot(faceNormal, normal(i0))).toBeGreaterThan(0);
    }
  });
});

describe("computeApproachGates()", () => {
  const gates = computeApproachGates(ksfo28LTunnelSpec);
  const threshold = Cartesian3.fromDegrees(
    ksfo28LTunnelSpec.thresholdLongitude,
    ksfo28LTunnelSpec.thresholdLatitude,
  );

  test("should place gates every 100 m from 2000 m to 100 m", () => {
    expect(gates.map((gate) => gate.thresholdDistanceMeters)).toEqual(
      Array.from({ length: 20 }, (_, i) => 2000 - 100 * i),
    );
  });

  test("should place gates at their distances from the threshold", () => {
    for (const gate of gates) {
      const ground = Cartographic.fromCartesian(gate.center);
      ground.height = 0;
      expect(
        Cartesian3.distance(Cartographic.toCartesian(ground), threshold),
      ).toBeCloseTo(gate.thresholdDistanceMeters, 0);
    }
  });

  test("should center gates on a 3° glideslope", () => {
    for (const gate of gates) {
      const expected =
        -29 +
        17 +
        gate.thresholdDistanceMeters * Math.tan(CesiumMath.toRadians(3));
      expect(Cartographic.fromCartesian(gate.center).height).toBeCloseTo(
        expected,
        6,
      );
    }
  });

  test("should orient each gate's +y axis toward the threshold", () => {
    for (const gate of gates) {
      const local = Matrix4.multiplyByPoint(
        Matrix4.inverseTransformation(gate.frame, new Matrix4()),
        threshold,
        new Cartesian3(),
      );
      // Nearly all of the offset to the threshold is along +y (and, owing to the glideslope, -z).
      expect(Math.abs(local.x)).toBeLessThan(0.5);
      expect(local.y).toBeGreaterThan(0.99 * gate.thresholdDistanceMeters);
    }
  });

  test("should shrink gates as they near the threshold", () => {
    for (let i = 1; i < gates.length; i++) {
      expect(gates[i].horizontalRadiusMeters).toBeLessThan(
        gates[i - 1].horizontalRadiusMeters,
      );
      expect(gates[i].verticalRadiusMeters).toBeLessThan(
        gates[i - 1].verticalRadiusMeters,
      );
    }
    expect(gates[0].horizontalRadiusMeters).toBe(45);
    expect(gates[gates.length - 1].horizontalRadiusMeters).toBeCloseTo(36);
  });
});
