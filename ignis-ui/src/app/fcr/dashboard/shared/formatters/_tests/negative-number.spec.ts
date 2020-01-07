import { negativeNumber } from "../negative-number";

describe("Negative Number Formatter", () => {
  it("should return a negative number in parenthesis", () => {
    expect(negativeNumber(-300)).toEqual("(300)");
  });

  it("should return a positive number untouched", () => {
    expect(negativeNumber(300)).toEqual(300);
  });
});
