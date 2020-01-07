import { HttpErrorResponse } from '@angular/common/http';
import { FailedAction } from '../../core/utilities/interfaces/errors.interface';
import { GetOneIdentifiableSuccessAction } from '../../core/utilities/interfaces/get-one-identifiable-action.interface';
import { IdentifiableAction } from '../../core/utilities/interfaces/identifiable-action.interface';
import { Identifiable } from '../../core/utilities/interfaces/indentifiable.interface';
import { PipelineStepTest } from '../interfaces/pipeline-step-test.interface';
import { UpdatePipelineStepTestRequest } from '../interfaces/update-pipeline-step-test-request.interface';

export enum ActionTypes {
  GetOne = '[Pipeline Step Test] Get One',
  GetOneSuccess = '[Pipeline Step Test] Get One Success',
  GetOneFail = '[Pipeline Step Test] Get One Fail',
  Update = '[Pipeline Step Test] Update',
  UpdateSuccess = '[Pipeline Step Test] Update Success',
  UpdateFail = '[Pipeline Step Test] Update Fail',
  Delete = '[Pipeline Step Test] Delete',
  DeleteSuccess = '[Pipeline Step Test] Delete Success',
  DeleteFail = '[Pipeline Step Test] Delete Fail',
  Run = '[Pipeline Step Test] Run',
  RunSuccess = '[Pipeline Step Test] Run Success',
  RunFail = '[Pipeline Step Test] Run Fail'
}

export class GetOne implements IdentifiableAction {
  readonly type = ActionTypes.GetOne;

  constructor(public id: number) {}
}

export class GetOneFail extends FailedAction implements IdentifiableAction {
  readonly type = ActionTypes.GetOneFail;

  constructor(public id: number, httpErrorResponse: HttpErrorResponse) {
    super(httpErrorResponse);
  }
}

export class GetOneSuccess implements GetOneIdentifiableSuccessAction {
  readonly type = ActionTypes.GetOneSuccess;

  constructor(public payload: PipelineStepTest) {}

  get id(): number {
    return this.payload.id;
  }
}

export class Update implements IdentifiableAction {
  readonly type = ActionTypes.Update;

  constructor(
    public request: UpdatePipelineStepTestRequest,
    public id: number
  ) {}
}

export class UpdateFail extends FailedAction implements IdentifiableAction {
  readonly type = ActionTypes.UpdateFail;

  constructor(public id: number, httpErrorResponse: HttpErrorResponse) {
    super(httpErrorResponse);
  }
}

export class UpdateSuccess implements GetOneIdentifiableSuccessAction {
  readonly type = ActionTypes.UpdateSuccess;

  constructor(public payload: PipelineStepTest) {}

  get id(): number {
    return this.payload.id;
  }
}

export class Delete implements IdentifiableAction {
  readonly type = ActionTypes.Delete;

  constructor(public id: number) {}
}

export class DeleteFail extends FailedAction implements IdentifiableAction {
  readonly type = ActionTypes.DeleteFail;

  constructor(public id: number, httpErrorResponse: HttpErrorResponse) {
    super(httpErrorResponse);
  }
}

export class DeleteSuccess implements IdentifiableAction {
  readonly type = ActionTypes.DeleteSuccess;

  constructor(public payload: Identifiable) {}

  get id(): number {
    return this.payload.id;
  }
}

export class Run implements IdentifiableAction {
  readonly type = ActionTypes.Run;

  constructor(public id: number) {}
}

export class RunFail extends FailedAction implements IdentifiableAction {
  readonly type = ActionTypes.RunFail;

  constructor(public id: number, httpErrorResponse: HttpErrorResponse) {
    super(httpErrorResponse);
  }
}

export class RunSuccess implements GetOneIdentifiableSuccessAction {
  readonly type = ActionTypes.RunSuccess;

  constructor(public id: number) {}
}

export type Actions =
  | GetOne
  | GetOneFail
  | GetOneSuccess
  | Update
  | UpdateFail
  | UpdateSuccess
  | Delete
  | DeleteFail
  | DeleteSuccess
  | Run
  | RunFail
  | RunSuccess;
