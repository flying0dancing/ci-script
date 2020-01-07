import * as ObjectUtilities from "./object.utilities";

describe("ObjectUtilities", () => {
  describe("isObject", () => {
    it("should return true if the provided argument is an object", () => {
      expect(ObjectUtilities.isObject({})).toEqual(true);
      expect(ObjectUtilities.isObject({ a: "b" })).toEqual(true);
    });

    it("should return false if the provided argument is not an object", () => {
      expect(ObjectUtilities.isObject(null)).toEqual(false);
      expect(ObjectUtilities.isObject(undefined)).toEqual(false);
      expect((<any>ObjectUtilities).isObject([])).toEqual(false);
      expect((<any>ObjectUtilities).isObject(function() {})).toEqual(false);
      expect((<any>ObjectUtilities).isObject(1)).toEqual(false);
      expect((<any>ObjectUtilities).isObject("A")).toEqual(false);
    });
  });
});
