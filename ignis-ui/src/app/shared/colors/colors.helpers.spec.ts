import * as ColorsHelpers from "./colors.helpers";

describe("ColorsHelpers", () => {
  it("should return a valid hex color value", () => {
    expect(ColorsHelpers.randomColor()).toMatch(/^#[A-F|0-9]{6}$/);
  });
});
