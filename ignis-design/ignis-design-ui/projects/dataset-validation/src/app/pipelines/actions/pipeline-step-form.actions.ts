import { Action } from '@ngrx/store';
import { PipelineStep, UpdatePipelineError } from '../interfaces/pipeline-step.interface';
import { GridDeleteAction } from '../../core/grid/interfaces/delete-params.interface';
import { FailedAction } from '../../core/utilities/interfaces/errors.interface';

export enum PipelineStepFormActionTypes {
  Reset = '[Pipeline Step Form] Reset',
  Post = '[Pipeline Step Form] Post',
  PostSuccessful = '[Pipeline Step Form] Post Success',
  PostFailed = '[Pipeline Step Form] Post Fail',
  Update = '[Pipeline Step Form] Update',
  UpdateSuccessful = '[Pipeline Step Form] Update Success',
  UpdateFailed = '[Pipeline Step Form] Update Fail',
  Delete = '[Pipeline Step Form] Delete',
  DeleteSuccessful = '[Pipeline Step Form] Delete Success',
  DeleteFail = '[Pipeline Step Form] Delete Fail'
}

export class Reset implements Action {
  readonly type = PipelineStepFormActionTypes.Reset;
}

export class Post implements Action {
  readonly type = PipelineStepFormActionTypes.Post;

  constructor(public pipelineId: number, public step: PipelineStep) {}
}

export class PostSuccessful implements Action {
  readonly type = PipelineStepFormActionTypes.PostSuccessful;

  constructor(public pipelineId: number) {}
}

export class PostFailed  {
  readonly type = PipelineStepFormActionTypes.PostFailed;

  constructor(public updatePipelineError: UpdatePipelineError) {}
}

export class Update implements Action {
  readonly type = PipelineStepFormActionTypes.Update;

  constructor(
    public pipelineId: number,
    public pipelineStepId: number,
    public step: PipelineStep
  ) {}
}

export class UpdateSuccessful implements Action {
  readonly type = PipelineStepFormActionTypes.UpdateSuccessful;

  constructor(public pipelineId: number) {}
}

export class UpdateFailed {
  readonly type = PipelineStepFormActionTypes.UpdateFailed;
  constructor(public updatePipelineError: UpdatePipelineError) {}
}

export class Delete implements GridDeleteAction {
  readonly type = PipelineStepFormActionTypes.Delete;

  constructor(public id: number, public pipelineId: number) {}
}

export class DeleteSuccess implements Action {
  readonly type = PipelineStepFormActionTypes.DeleteSuccessful;
}

export class DeleteFail extends FailedAction {
  readonly type = PipelineStepFormActionTypes.DeleteFail;
}

export type PipelineStepFormActions =
  | Reset
  | Post
  | PostSuccessful
  | PostFailed
  | Update
  | UpdateSuccessful
  | UpdateFailed
  | Delete
  | DeleteSuccess
  | DeleteFail;
