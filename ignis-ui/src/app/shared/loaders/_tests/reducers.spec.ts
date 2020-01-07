import {
  selectHasPendingHttpRequests,
  selectHttpRequestCount,
  selectLoaders
} from "../reducers";

describe("reducer selectors", () => {
  describe("selectLoaders", () => {
    it("should select the loaders state slice", () => {
      const state = {
        loaders: {
          httpRequestCount: 0
        }
      };

      const expected = {
        httpRequestCount: 0
      };

      expect(selectLoaders(state)).toEqual(expected);
    });
  });

  describe("selectHttpRequestCount", () => {
    it("should select the loaders httpRequestCount state slice", () => {
      const state = {
        loaders: {
          httpRequestCount: 0
        }
      };

      const expected = 0;

      expect(selectHttpRequestCount(state)).toEqual(expected);
    });
  });

  describe("selectHasPendingHttpRequests", () => {
    it("should return true when http request count is greater than zero", () => {
      const state = {
        loaders: {
          httpRequestCount: 1
        }
      };

      expect(selectHasPendingHttpRequests(state)).toBe(true);
    });

    it("should return false when http request count less than 1", () => {
      const state = {
        loaders: {
          httpRequestCount: 0
        }
      };

      expect(selectHasPendingHttpRequests(state)).toBe(false);
    });
  });
});
