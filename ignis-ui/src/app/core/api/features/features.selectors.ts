import { Feature } from "@/core/api/features/features.interfaces";
import {
  FeaturesState,
  initialFeaturesState,
  NAMESPACE,
  State
} from "@/core/api/features/features.reducer";
import {
  createFeatureSelector,
  createSelector,
  MemoizedSelector
} from "@ngrx/store";

export function getByNameSelector(name: string) {
  return getFeaturesByNameFactory(name, NAMESPACE);
}

export const getFeaturesState = createFeatureSelector<State>(NAMESPACE);

export const getFeaturesFactory = (key: string) =>
  createSelector(
    getFeaturesState,
    (state: State) => state[key]
  );

export const getFeaturesCollectionFactory = (key: string) =>
  createSelector(
    getFeaturesFactory(key),
    (state: FeaturesState) =>
      (state && state.collection) || initialFeaturesState.collection
  );

export function getFeaturesByNameFactory(
  name: string,
  key: string
): MemoizedSelector<object, Feature> {
  return createSelector(
    getFeaturesFactory(key),
    (state: FeaturesState) => {
      const features: Feature[] =
        (state && (state.collection || initialFeaturesState.collection)) || [];
      return features.find(feature => feature.name === name);
    }
  );
}

export const getFeaturesGetStateFactory = (key: string) =>
  createSelector(
    getFeaturesFactory(key),
    (state: FeaturesState) => (state && state.get) || initialFeaturesState.get
  );

export const getFeaturesGetLoadingStateFactory = (key: string) =>
  createSelector(
    getFeaturesFactory(key),
    (state: FeaturesState) =>
      (state && state.get.loading) || initialFeaturesState.get.loading
  );
