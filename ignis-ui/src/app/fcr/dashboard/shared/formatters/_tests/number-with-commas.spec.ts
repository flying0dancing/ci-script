import { numberWithCommas } from "../number-with-commas";

describe("Value Formatter: Number With Commas", () => {
  it("should return a number with commas", () => {
    expect(numberWithCommas(1000.2)).toEqual("1,000.2");
    expect(numberWithCommas(-5000000)).toEqual("-5,000,000");
  });

  it("should return a number without commas", () => {
    expect(numberWithCommas(10)).toEqual("10");
    expect(numberWithCommas(-100.32)).toEqual("-100.32");
  });

  it("should return the original value if argument is not a number", () => {
    const stringValue = "Testing";
    const nullValue = null;
    const undefinedValue = undefined;
    const numberWithCommasNoType: any = numberWithCommas;

    expect(numberWithCommasNoType(stringValue)).toEqual(stringValue);
    expect(numberWithCommasNoType(nullValue)).toEqual(nullValue);
    expect(numberWithCommasNoType(undefinedValue)).toEqual(undefinedValue);
  });
});
