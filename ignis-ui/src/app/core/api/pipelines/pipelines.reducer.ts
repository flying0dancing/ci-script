import {
  Pipeline,
  PipelineDownstream,
  PipelineInvocation,
  SchemaDetails
} from "@/core/api/pipelines/pipelines.interfaces";
import * as PipelinesActions from "./pipelines.actions";

export const NAMESPACE = "pipelines";

export interface PipelinesState {
  pipelines: Pipeline[];
  downstreams: PipelineDownstream[];
  invocations: PipelineInvocation[];
  requiredSchemas: SchemaDetails[];
  get: {
    loading: boolean;
    error: boolean;
  };
  getDownstreams: {
    loading: boolean;
    error: boolean;
  };
  getRequiredSchemas: {
    loading: boolean;
    error: boolean;
  };
  getInvocations: {
    loading: boolean;
    error: boolean;
  };
}

export interface State {
  [key: string]: PipelinesState;
}

export const initialPipelinesState: PipelinesState = {
  pipelines: [],
  downstreams: [],
  invocations: [],
  requiredSchemas: [],
  get: {
    loading: false,
    error: false
  },
  getDownstreams: {
    loading: false,
    error: false
  },
  getRequiredSchemas: {
    loading: false,
    error: false
  },
  getInvocations: {
    loading: false,
    error: false
  }
};

export function pipelinesReducer(
  state: PipelinesState = initialPipelinesState,
  action: PipelinesActions.Types
) {
  switch (action.type) {
    case PipelinesActions.GET:
      return { ...state, get: { ...state.get, loading: true } };

    case PipelinesActions.GET_SUCCESS:
      return {
        ...state,
        get: { ...state.get, loading: false },
        pipelines: action.payload.pipelines
      };

    case PipelinesActions.GET_FAIL:
      return { ...state, get: { loading: false, error: true } };

    case PipelinesActions.GET_DOWNSTREAMS:
      return {
        ...state,
        getDownstreams: { ...state.getDownstreams, loading: true }
      };

    case PipelinesActions.GET_DOWNSTREAMS_SUCCESS:
      return {
        ...state,
        getDownstreams: { ...state.getDownstreams, loading: false },
        downstreams: action.payload.pipelineDownstreams
      };

    case PipelinesActions.GET_DOWNSTREAMS_FAIL:
      return { ...state, getDownstreams: { loading: false, error: true } };

    case PipelinesActions.GET_REQUIRED_SCHEMAS:
      return { ...state, getRequiredSchemas: { ...state.get, loading: true } };

    case PipelinesActions.GET_REQUIRED_SCHEMAS_SUCCESS:
      return {
        ...state,
        getRequiredSchemas: { ...state.get, loading: false },
        requiredSchemas: action.payload.requiredSchemas
      };

    case PipelinesActions.GET_REQUIRED_SCHEMAS_FAIL:
      return { ...state, getRequiredSchemas: { loading: false, error: true } };

    case PipelinesActions.GET_INVOCATIONS:
      return {
        ...state,
        getInvocations: { ...state.getInvocations, loading: true }
      };

    case PipelinesActions.GET_INVOCATIONS_SUCCESS:
      return {
        ...state,
        getInvocations: { ...state.getInvocations, loading: false },
        invocations: action.payload.pipelineInvocations
      };

    case PipelinesActions.GET_INVOCATIONS_FAIL:
      return { ...state, getInvocations: { loading: false, error: true } };

    case PipelinesActions.EMPTY:
      return { ...initialPipelinesState };

    default:
      return state;
  }
}

export function reducer(state: State = {}, action: any) {
  switch (action.type) {
    case PipelinesActions.GET:
    case PipelinesActions.GET_SUCCESS:
    case PipelinesActions.GET_FAIL:
    case PipelinesActions.GET_DOWNSTREAMS:
    case PipelinesActions.GET_DOWNSTREAMS_SUCCESS:
    case PipelinesActions.GET_DOWNSTREAMS_FAIL:
    case PipelinesActions.GET_REQUIRED_SCHEMAS:
    case PipelinesActions.GET_REQUIRED_SCHEMAS_SUCCESS:
    case PipelinesActions.GET_REQUIRED_SCHEMAS_FAIL:
    case PipelinesActions.GET_INVOCATIONS:
    case PipelinesActions.GET_INVOCATIONS_SUCCESS:
    case PipelinesActions.GET_INVOCATIONS_FAIL:
    case PipelinesActions.EMPTY:
      return {
        ...state,
        [action.payload.reducerMapKey]: pipelinesReducer(
          state[action.payload.reducerMapKey],
          action
        )
      };
    default:
      return state;
  }
}
