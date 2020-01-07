import { Action } from '@ngrx/store';
import { GridDeleteAction } from '../../core/grid/interfaces/delete-params.interface';
import { FailedAction } from '../../core/utilities/interfaces/errors.interface';
import { GetAllIdentifiableSuccessAction } from '../../core/utilities/interfaces/get-all-identifiable-action.interface';
import { GetOneIdentifiableSuccessAction } from '../../core/utilities/interfaces/get-one-identifiable-action.interface';
import { CreatePipelineRequest } from '../interfaces/create-pipeline-request.interface';
import { Pipeline, PipelineEdge } from '../interfaces/pipeline.interface';
import { UpdatePipelineRequest } from '../interfaces/update-pipeline-request.interface';

export enum PipelineActionTypes {
  GetOne = '[Pipeline] Get One',
  GetOneSuccess = '[Pipeline] Get One Success',
  GetOneFail = '[Pipeline] Get One Fail',

  GetAll = '[Pipeline] Get All',
  GetAllSuccessful = '[Pipeline] Get All Success',
  GetAllFailed = '[Pipeline] Get All Fail',

  Create = '[Pipeline] Create',
  CreateSuccessful = '[Pipeline] Create Success',
  CreateFailed = '[Pipeline] Create Fail',

  Update = '[Pipeline] Update',
  UpdateSuccessful = '[Pipeline] Update Success',
  UpdateFailed = '[Pipeline] Update Fail',

  GetPipelineEdges = '[Pipeline] GetPipelineEdges',
  GetPipelineEdgesSuccessful = '[Pipeline] GetPipelineEdges Success',
  GetPipelineEdgesFailed = '[Pipeline] GetPipelineEdges Fail',

  Delete = '[Pipeline] Delete',
  DeleteSuccessful = '[Pipeline] Delete Success',
  DeleteFailed = '[Pipeline] Delete Fail'
}

export class GetOne implements Action {
  readonly type = PipelineActionTypes.GetOne;

  constructor(public id: number) {}
}

export class GetOneFail extends FailedAction {
  readonly type = PipelineActionTypes.GetOneFail;
}

export class GetOneSuccess implements GetOneIdentifiableSuccessAction {
  readonly type = PipelineActionTypes.GetOneSuccess;

  constructor(public payload: Pipeline) {}

  id = this.payload.id;
}

export class GetAll implements Action {
  readonly type = PipelineActionTypes.GetAll;
}

export class GetAllFail extends FailedAction {
  readonly type = PipelineActionTypes.GetAllFailed;
}

export class GetAllSuccess implements GetAllIdentifiableSuccessAction {
  readonly type = PipelineActionTypes.GetAllSuccessful;

  constructor(public payload: Pipeline[]) {}

  get ids(): number[] {
    return this.payload.map(pipeline => pipeline.id);
  }
}

export class Create implements Action {
  readonly type = PipelineActionTypes.Create;

  constructor(public request: CreatePipelineRequest) {}
}

export class CreateFail extends FailedAction {
  readonly type = PipelineActionTypes.CreateFailed;
}

export class CreateSuccess implements Action {
  readonly type = PipelineActionTypes.CreateSuccessful;
}

export class Update implements Action {
  readonly type = PipelineActionTypes.Update;

  constructor(public id: number, public request: UpdatePipelineRequest) {}
}

export class UpdateFail extends FailedAction {
  readonly type = PipelineActionTypes.UpdateFailed;
}

export class UpdateSuccess implements Action {
  readonly type = PipelineActionTypes.UpdateSuccessful;
}

export class GetPipelineEdges implements Action {
  readonly type = PipelineActionTypes.GetPipelineEdges;

  constructor(public id: number) {}
}

export class GetPipelineEdgesFail extends FailedAction {
  readonly type = PipelineActionTypes.GetPipelineEdgesFailed;
}

export class GetPipelineEdgesSuccess implements Action {
  readonly type = PipelineActionTypes.GetPipelineEdgesSuccessful;

  constructor(public edges: PipelineEdge[]) {}
}

export class Delete implements GridDeleteAction {
  readonly type = PipelineActionTypes.Delete;

  constructor(public id: number) {}
}

export class DeleteFail extends FailedAction {
  readonly type = PipelineActionTypes.DeleteFailed;
}

export class DeleteSuccess implements Action {
  readonly type = PipelineActionTypes.DeleteSuccessful;

  constructor(public id: number) {}
}

export type PipelineActions =
  | GetOne
  | GetOneSuccess
  | GetOneFail
  | GetAll
  | GetAllFail
  | GetAllSuccess
  | Create
  | CreateSuccess
  | CreateFail
  | Update
  | UpdateSuccess
  | UpdateFail
  | GetPipelineEdges
  | GetPipelineEdgesSuccess
  | GetPipelineEdgesFail
  | Delete
  | DeleteSuccess
  | DeleteFail;
