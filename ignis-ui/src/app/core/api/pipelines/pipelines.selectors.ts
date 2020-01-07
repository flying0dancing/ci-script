import { createFeatureSelector, createSelector } from "@ngrx/store";
import {
  initialPipelinesState,
  PipelinesState,
  State
} from "./pipelines.reducer";

export const NAMESPACE = "pipelines";

export const getPipelinesState = createFeatureSelector<State>(NAMESPACE);

export const getPipelinesFactory = (key: string) =>
  createSelector(
    getPipelinesState,
    (state: State) => state[key]
  );

export const getPipelinesCollectionFactory = (key: string) =>
  createSelector(
    getPipelinesFactory(key),
    (state: PipelinesState) =>
      (state && state.pipelines) || initialPipelinesState.pipelines
  );

export const getPipelineDownstreamsCollectionFactory = (key: string) =>
  createSelector(
    getPipelinesFactory(key),
    (state: PipelinesState) =>
      (state && state.downstreams) || initialPipelinesState.downstreams
  );

export const getPipelinesGetStateFactory = (key: string) =>
  createSelector(
    getPipelinesFactory(key),
    (state: PipelinesState) => (state && state.get) || initialPipelinesState.get
  );

export const getPipelinesGetLoadingStateFactory = (key: string) =>
  createSelector(
    getPipelinesFactory(key),
    (state: PipelinesState) =>
      (state && state.get.loading) || initialPipelinesState.get.loading
  );

export const getRequiredSchemasFactory = (key: string) =>
  createSelector(
    getPipelinesFactory(key),
    (state: PipelinesState) =>
      (state && state.requiredSchemas) || initialPipelinesState.requiredSchemas
  );

export const getRequiredSchemasLoadingFactory = (key: string) =>
  createSelector(
    getPipelinesFactory(key),
    (state: PipelinesState) =>
      (state && state.getRequiredSchemas.loading) ||
      initialPipelinesState.getRequiredSchemas.loading
  );

export const getPipelineInvocationsCollectionFactory = (key: string) =>
  createSelector(
    getPipelinesFactory(key),
    (state: PipelinesState) =>
      (state && state.invocations) || initialPipelinesState.invocations
  );

export const getPipelineInvocationsGetLoadingStateFactory = (key: string) =>
  createSelector(
    getPipelinesFactory(key),
    (state: PipelinesState) =>
      (state && state.getInvocations.loading) ||
      initialPipelinesState.getInvocations.loading
  );

export const getPipelines = getPipelinesCollectionFactory(NAMESPACE);
export const getPipelineDownstreams = getPipelineDownstreamsCollectionFactory(
  NAMESPACE
);
export const getPipelineInvocations = getPipelineInvocationsCollectionFactory(
  NAMESPACE
);
export const getRequiredSchemas = getRequiredSchemasFactory(NAMESPACE);
export const getRequiredSchemasLoading = getRequiredSchemasLoadingFactory(
  NAMESPACE
);
export const getPipelinesLoading = getPipelinesGetLoadingStateFactory(
  NAMESPACE
);
export const getPipelineInvocationsLoading = getPipelineInvocationsGetLoadingStateFactory(
  NAMESPACE
);
