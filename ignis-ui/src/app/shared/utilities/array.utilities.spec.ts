import * as ArrayUtilities from "./array.utilities";
import { flatten } from "./array.utilities";

describe("ArrayUtilities", () => {
  describe("isArray", () => {
    it("should return true if the provided argument is an array", () => {
      expect(ArrayUtilities.isArray([])).toEqual(true);
      expect(ArrayUtilities.isArray(["A"])).toEqual(true);
    });

    it("should return false if the provided argument is not an array", () => {
      expect(ArrayUtilities.isArray(null)).toEqual(false);
      expect(ArrayUtilities.isArray(undefined)).toEqual(false);
      expect((<any>ArrayUtilities).isArray(1)).toEqual(false);
      expect((<any>ArrayUtilities).isArray("A")).toEqual(false);
    });
  });

  describe("flatten", () => {
    it("should flatten nested arrays", () => {
      const array = [
        0,
        1,
        2,
        [3],
        [4, 5],
        [6, [7, 8, 9]],
        [10, [11, [12, 13]]]
      ];

      const flattened = flatten(array);

      expect(flattened).toEqual([0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13]);
    });
  });
});
