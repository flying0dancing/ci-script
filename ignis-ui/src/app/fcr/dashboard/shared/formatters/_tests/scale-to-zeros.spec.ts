import { scaleToZeros } from "../scale-to-zeros";

describe("scaleToZeros", () => {
  it("should the correct number of comma separated zeros", () => {
    expect(scaleToZeros(6)).toBe("000,000's");
    expect(scaleToZeros(-6)).toBe("000,000's");
  });
});
