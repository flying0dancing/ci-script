import { Action } from '@ngrx/store';
import { PipelineStepTest } from '../interfaces/pipeline-step-test.interface';
import { CreatePipelineStepTestRequest } from '../interfaces/create-pipeline-step-test-request.interface';
import { FailedAction } from '../../core/utilities/interfaces/errors.interface';
import { GetAllIdentifiableSuccessAction } from '../../core/utilities/interfaces/get-all-identifiable-action.interface';

export enum ActionTypes {
  GetAll = '[Pipeline Step Tests] Get All',
  GetAllSuccess = '[Pipeline Step Tests] Get All Success',
  GetAllFail = '[Pipeline Step Tests] Get All Fail',
  Create = '[Pipeline Step Tests] Create',
  CreateSuccess = '[Pipeline Step Tests] Create Success',
  CreateFail = '[Pipeline Step Tests] Create Fail'
}

export class GetAll implements Action {
  readonly type = ActionTypes.GetAll;

  constructor(public pipelineId: number) {}
}

export class GetAllFail extends FailedAction {
  readonly type = ActionTypes.GetAllFail;
}

export class GetAllSuccess implements GetAllIdentifiableSuccessAction {
  readonly type = ActionTypes.GetAllSuccess;

  constructor(public payload: PipelineStepTest[]) {}

  get ids(): number[] {
    return this.payload.map(pipelineStepTest => pipelineStepTest.id);
  }
}

export class Create implements Action {
  readonly type = ActionTypes.Create;

  constructor(
    public request: CreatePipelineStepTestRequest,
    public pipelineId: number
  ) {}
}

export class CreateFail extends FailedAction {
  readonly type = ActionTypes.CreateFail;
}

export class CreateSuccess {
  readonly type = ActionTypes.CreateSuccess;
}

export type Actions =
  | GetAll
  | GetAllFail
  | GetAllSuccess
  | Create
  | CreateSuccess
  | CreateFail;
