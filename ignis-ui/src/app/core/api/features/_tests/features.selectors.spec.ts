import { IgnisFeature } from "@/core/api/features/features.enum";
import { Feature } from "@/core/api/features/features.interfaces";
import {
  FeaturesState,
  initialFeaturesState,
  NAMESPACE
} from "../features.reducer";
import * as FeaturesSelectors from "../features.selectors";

describe("Features Selectors", () => {
  const key = "testing";

  describe("getState", () => {
    it(`should select the ${NAMESPACE} state`, () => {
      const state = { [NAMESPACE]: {} };

      expect(FeaturesSelectors.getFeaturesState(state)).toEqual({});
    });

    it(`should return undefined if the ${NAMESPACE} state does not exist`, () => {
      expect(FeaturesSelectors.getFeaturesState({})).toBeUndefined();
    });
  });

  describe("getFactory", () => {
    it(`should select the ${NAMESPACE} state using the reducer key`, () => {
      const state = {
        [NAMESPACE]: {
          [key]: initialFeaturesState
        }
      };

      expect(FeaturesSelectors.getFeaturesFactory(key)(state)).toEqual(
        initialFeaturesState
      );
    });

    it(`should select the ${NAMESPACE} state using the reducer key`, () => {
      const state = { [NAMESPACE]: {} };

      expect(FeaturesSelectors.getFeaturesFactory(key)(state)).toBeUndefined();
    });
  });

  describe("getCollectionFactory ", () => {
    it(`should select the collection of the ${NAMESPACE} state using the reducer key`, () => {
      const state = {
        [NAMESPACE]: {
          [key]: initialFeaturesState
        }
      };

      expect(
        FeaturesSelectors.getFeaturesCollectionFactory(key)(state)
      ).toEqual(initialFeaturesState.collection);
    });

    it(`should return the collection initial state if there is no collection in the ${NAMESPACE} state using the reducer key`, () => {
      const state = { [NAMESPACE]: {} };

      expect(
        FeaturesSelectors.getFeaturesCollectionFactory(key)(state)
      ).toEqual(initialFeaturesState.collection);
    });
  });

  describe("getGetStateFactory ", () => {
    it(`should select the get property of the ${NAMESPACE} state using the reducer key`, () => {
      const state = {
        [NAMESPACE]: {
          [key]: initialFeaturesState
        }
      };

      expect(FeaturesSelectors.getFeaturesGetStateFactory(key)(state)).toEqual(
        initialFeaturesState.get
      );
    });

    it(`should return the initial get state if there is no get property in the ${NAMESPACE} state using the reducer key`, () => {
      const state = { [NAMESPACE]: {} };

      expect(FeaturesSelectors.getFeaturesGetStateFactory(key)(state)).toEqual(
        initialFeaturesState.get
      );
    });
  });

  describe("getFeaturesGetLoadingStateFactory", () => {
    it(`should select the "get.loading" property of the ${NAMESPACE} state using the reducer key`, () => {
      const state = {
        [NAMESPACE]: {
          [key]: initialFeaturesState
        }
      };

      expect(
        FeaturesSelectors.getFeaturesGetLoadingStateFactory(key)(state)
      ).toEqual(initialFeaturesState.get.loading);
    });

    it(`should return the "get.loading" initial state if there is no get property
      in the ${NAMESPACE} state using the reducer key`, () => {
      const state = { [NAMESPACE]: {} };

      expect(
        FeaturesSelectors.getFeaturesGetLoadingStateFactory(key)(state)
      ).toEqual(initialFeaturesState.get.loading);
    });
  });

  describe("getFeaturesByNameFactory", () => {
    it(`should select the nothing in ${NAMESPACE} state as collection is empty`, () => {
      const state = {
        [NAMESPACE]: {
          [key]: initialFeaturesState
        }
      };

      expect(
        FeaturesSelectors.getFeaturesByNameFactory(
          IgnisFeature.APPEND_DATASETS,
          key
        )(state)
      ).toBeUndefined();
    });

    it(`should return null if feature not found for name`, () => {
      const featuresState: FeaturesState = {
        ...initialFeaturesState,
        collection: [
          { name: IgnisFeature.APPEND_DATASETS, active: true }
        ]
      };

      const state = {
        [NAMESPACE]: {
          [key]: featuresState
        }
      };
      expect(
        FeaturesSelectors.getFeaturesByNameFactory("UNDEFINED", key)(state)
      ).toBeUndefined();
    });

    it(`should return feature if found by name`, () => {
      const feature: Feature = {
        name: IgnisFeature.APPEND_DATASETS,
        active: true
      };
      const featuresState: FeaturesState = {
        ...initialFeaturesState,
        collection: [feature]
      };

      const state = {
        [NAMESPACE]: {
          [key]: featuresState
        }
      };
      expect(
        FeaturesSelectors.getFeaturesByNameFactory(
          IgnisFeature.APPEND_DATASETS,
          key
        )(state)
      ).toEqual(feature);
    });
  });
});
