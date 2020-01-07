import { stringNumberWithCommas } from "../string-number-with-commas";

describe("Value Formatter: String Number With Commas", () => {
  it("should return a number with commas", () => {
    expect(stringNumberWithCommas("1000.20")).toEqual("1,000.20");
    expect(stringNumberWithCommas("-5000000")).toEqual("-5,000,000");
  });

  it("should return a number without commas", () => {
    expect(stringNumberWithCommas("10")).toEqual("10");
    expect(stringNumberWithCommas("-100.32")).toEqual("-100.32");
  });
});
