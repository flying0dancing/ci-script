import { percentage } from "../percentage";

describe("Percentage Formatter", () => {
  it("should format the nubmer with a percentage symbol", () => {
    expect(percentage(40)).toEqual("40.00%");
  });

  it("should return the value if NaN", () => {
    expect(percentage(null)).toEqual(null);
  });
});
