import * as FeaturesActions from "./features.actions";
import { Feature } from "./features.interfaces";

export const NAMESPACE = "features";

export interface FeaturesState {
  collection: Feature[];
  get: {
    loading: boolean;
    error: boolean;
  };
}

export interface State {
  [key: string]: FeaturesState;
}

export const initialFeaturesState: FeaturesState = {
  collection: [],
  get: {
    loading: false,
    error: false
  }
};

export function featuresReducer(
  state: FeaturesState = initialFeaturesState,
  action: FeaturesActions.Types
) {
  switch (action.type) {
    case FeaturesActions.GET:
      return { ...state, get: { ...state.get, loading: true } };

    case FeaturesActions.GET_SUCCESS:
      return {
        ...state,
        get: { ...state.get, loading: false },
        collection: action.payload.features
      };

    case FeaturesActions.GET_FAIL:
      return { ...state, get: { loading: false, error: true } };

    case FeaturesActions.EMPTY:
      return { ...initialFeaturesState };

    default:
      return state;
  }
}

export function reducer(state: State = {}, action: any) {
  switch (action.type) {
    case FeaturesActions.GET:
    case FeaturesActions.GET_SUCCESS:
    case FeaturesActions.GET_FAIL:
    case FeaturesActions.EMPTY:
      return {
        ...state,
        [action.payload.reducerMapKey]: featuresReducer(
          state[action.payload.reducerMapKey],
          action
        )
      };
    default:
      return state;
  }
}
