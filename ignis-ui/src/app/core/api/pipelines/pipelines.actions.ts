import { Action } from "@ngrx/store";
import * as Interfaces from "./pipelines.interfaces";

export const GET = "pipelines/get";
export const GET_SUCCESS = "pipelines/get-success";
export const GET_FAIL = "pipelines/get-fail";
export const GET_DOWNSTREAMS = "pipelines/get-downstreams";
export const GET_DOWNSTREAMS_SUCCESS = "pipelines/get-downstreams-success";
export const GET_DOWNSTREAMS_FAIL = "pipelines/get-downstreams-fail";
export const GET_REQUIRED_SCHEMAS = "pipelines/get-requiredSchemas";
export const GET_REQUIRED_SCHEMAS_SUCCESS =
  "pipelines/get-requiredSchemas-success";
export const GET_REQUIRED_SCHEMAS_FAIL = "pipelines/get-requiredSchemas-fail";
export const GET_INVOCATIONS = "pipelines/get-invocations";
export const GET_INVOCATIONS_SUCCESS = "pipelines/get-invocations-success";
export const GET_INVOCATIONS_FAIL = "pipelines/get-invocations-fail";
export const EMPTY = "pipelines/empty";

export class Get implements Action {
  readonly type = GET;

  constructor(public payload: { reducerMapKey: string }) {}
}

export class GetSuccess implements Action {
  readonly type = GET_SUCCESS;

  constructor(
    public payload: { reducerMapKey: string; pipelines: Interfaces.Pipeline[] }
  ) {}
}

export class GetFail implements Action {
  readonly type = GET_FAIL;

  constructor(public payload: { reducerMapKey: string }) {}
}

export class GetDownstreams implements Action {
  readonly type = GET_DOWNSTREAMS;

  constructor(public payload: { reducerMapKey: string }) {}
}

export class GetDownstreamsSuccess implements Action {
  readonly type = GET_DOWNSTREAMS_SUCCESS;

  constructor(
    public payload: {
      reducerMapKey: string;
      pipelineDownstreams: Interfaces.PipelineDownstream[];
    }
  ) {}
}

export class GetDownstreamsFail implements Action {
  readonly type = GET_DOWNSTREAMS_FAIL;

  constructor(public payload: { reducerMapKey: string }) {}
}

export class GetRequiredSchemas implements Action {
  readonly type = GET_REQUIRED_SCHEMAS;

  constructor(public payload: { reducerMapKey: string; pipelineId: number }) {}
}

export class GetRequiredSchemasSuccess implements Action {
  readonly type = GET_REQUIRED_SCHEMAS_SUCCESS;

  constructor(
    public payload: {
      reducerMapKey: string;
      requiredSchemas: Interfaces.SchemaDetails[];
    }
  ) {}
}

export class GetRequiredSchemasFail implements Action {
  readonly type = GET_REQUIRED_SCHEMAS_FAIL;

  constructor(public payload: { reducerMapKey: string }) {}
}

export class GetInvocations implements Action {
  readonly type = GET_INVOCATIONS;

  constructor(
    public payload: { reducerMapKey: string; jobExecutionId?: number }
  ) {}
}

export class GetInvocationsSuccess implements Action {
  readonly type = GET_INVOCATIONS_SUCCESS;

  constructor(
    public payload: {
      reducerMapKey: string;
      pipelineInvocations: Interfaces.PipelineInvocation[];
    }
  ) {}
}

export class GetInvocationsFail implements Action {
  readonly type = GET_INVOCATIONS_FAIL;

  constructor(public payload: { reducerMapKey: string }) {}
}

export class Empty implements Action {
  readonly type = EMPTY;

  constructor(public payload: { reducerMapKey: string }) {}
}

export type Types =
  | Get
  | GetSuccess
  | GetFail
  | GetDownstreams
  | GetDownstreamsSuccess
  | GetDownstreamsFail
  | GetRequiredSchemas
  | GetRequiredSchemasSuccess
  | GetRequiredSchemasFail
  | GetInvocations
  | GetInvocationsSuccess
  | GetInvocationsFail
  | Empty;
