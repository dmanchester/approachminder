import { formatNumber } from "../lib/UI";

import { describe, expect, test } from "vitest";

describe("formatNumber()", () => {

  test("should round when the number has more digits than requested", () => {
    expect(formatNumber(1.2, 0)).toBe("1");
    expect(formatNumber(1.234, 2)).toBe("1.23");
    expect(formatNumber(1.235, 2)).toBe("1.24");
    expect(formatNumber(1.245, 2)).toBe("1.25");
  });

  test("should right-pad with zeros when the number has fewer digits than requested", () => {
    expect(formatNumber(1.2, 2)).toBe("1.20");
  });

  test("should include thousands separators", () => {
    expect(formatNumber(1234.5, 1)).toBe("1,234.5");
  });

  test("should handle negative numbers", () => {
    expect(formatNumber(-1.234, 2)).toBe("-1.23");
  });

  test("should handle zero", () => {
    expect(formatNumber(0, 0)).toBe("0");
  });

  test("should handle null", () => {
    expect(formatNumber(null, 1)).toBe("null");
  });

  test("should handle undefined", () => {
    expect(formatNumber(undefined, 1)).toBe("undefined");
  });
});
