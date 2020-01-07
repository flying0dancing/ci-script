import { HttpErrorResponse } from '@angular/common/http';
import { FailedAction } from '../../core/utilities/interfaces/errors.interface';
import { CreateInputDataRowRequest } from '../interfaces/create-input-data-row-request.interface';
import { CreateExpectedDataRowRequest } from '../interfaces/created-expected-data-row-request.interface';
import { InputData, OutputData, PipelineStepInputRows } from '../interfaces/pipeline-step-test.interface';
import { UpdateRowCellDataRequest } from '../interfaces/update-row-cell-data-request.interface';

export enum ActionTypes {
  GetTestInputRows = '[Pipeline Step Test Row] Get Input Rows',
  GetTestInputRowsSuccess = '[Pipeline Step Test Row] Get Input Rows Success',
  GetTestInputRowsFail = '[Pipeline Step Test Row] Get Input Rows Fail',
  CreateInputDataRow = '[Pipeline Step Test Row] Create Input Data Row',
  CreateInputDataRowSuccess = '[Pipeline Step Test Row] Create Input Data Row Success',
  CreateInputDataRowFail = '[Pipeline Step Test Row] Create Input Data Row Fail',
  DeleteInputDataRow = '[Pipeline Step Test Row] Delete Input Data Row',
  DeleteInputDataRowSuccess = '[Pipeline Step Test Row] Delete Input Data Row Success',
  DeleteInputDataRowFail = '[Pipeline Step Test Row] Delete Input Data Row Fail',
  UpdateInputRowCellData = '[Pipeline Step Test Row] Update Input Row Cell Data',
  UpdateInputRowCellDataSuccess = '[Pipeline Step Test Row] Update Input Row Cell Data Success',
  UpdateInputRowCellDataFail = '[Pipeline Step Test Row] Update Input Row Cell Data Fail',

  GetTestExpectedRows = '[Pipeline Step Test Row] Get Expected Rows',
  GetTestExpectedRowsSuccess = '[Pipeline Step Test Row] Get Expected Rows Success',
  GetTestExpectedRowsFail = '[Pipeline Step Test Row] Get Expected Rows Fail',
  CreateExpectedDataRow = '[Pipeline Step Test Row] Create Expected Data Row',
  CreateExpectedDataRowSuccess = '[Pipeline Step Test Row] Create Expected Data Row Success',
  CreateExpectedDataRowFail = '[Pipeline Step Test Row] Create Input Expected Row Fail',
  DeleteExpectedDataRow = '[Pipeline Step Test Row] Delete Expected Data Row',
  DeleteExpectedDataRowSuccess = '[Pipeline Step Test Row] Delete Expected Data Row Success',
  DeleteExpectedDataRowFail = '[Pipeline Step Test Row] Delete Expected Data Row Fail',
  UpdateExpectedRowCellData = '[Pipeline Step Test Row] Update Expected Row Cell Data',
  UpdateExpectedRowCellDataSuccess = '[Pipeline Step Test Row] Update Expected Row Cell Data Success',
  UpdateExpectedRowCellDataFail = '[Pipeline Step Test Row] Update Expected Row Cell Data Fail',
}

export class GetTestInputRows {
  readonly type = ActionTypes.GetTestInputRows;

  constructor(public id: number) {}
}

export class GetTestInputRowsSuccess {
  readonly type = ActionTypes.GetTestInputRowsSuccess;

  constructor(public inputRows: PipelineStepInputRows) {}
}

export class GetTestInputRowsFail extends FailedAction {
  readonly type = ActionTypes.GetTestInputRowsFail;
}

export class CreateInputDataRow {
  readonly type = ActionTypes.CreateInputDataRow;

  constructor(public request: CreateInputDataRowRequest, public id: number) {}
}

export class CreateInputDataRowFail extends FailedAction {
  readonly type = ActionTypes.CreateInputDataRowFail;

  constructor(public id: number, httpErrorResponse: HttpErrorResponse) {
    super(httpErrorResponse);
  }
}

export class CreateInputDataRowSuccess {
  readonly type = ActionTypes.CreateInputDataRowSuccess;

  constructor(public id: number) {}
}

export class DeleteInputDataRow {
  readonly type = ActionTypes.DeleteInputDataRow;

  constructor(public id: number, public rowId: number) {}
}

export class DeleteInputDataRowFail extends FailedAction {
  readonly type = ActionTypes.DeleteInputDataRowFail;

  constructor(public id: number, httpErrorResponse: HttpErrorResponse) {
    super(httpErrorResponse);
  }
}

export class DeleteInputDataRowSuccess {
  readonly type = ActionTypes.DeleteInputDataRowSuccess;

  constructor(public id: number) {}
}

export class UpdateInputRowCellData {
  readonly type = ActionTypes.UpdateInputRowCellData;

  constructor(
    public id: number,
    public rowId: number,
    public rowCellDataId: number,
    public request: UpdateRowCellDataRequest
  ) {}
}

export class UpdateInputRowCellDataSuccess {
  readonly type = ActionTypes.UpdateInputRowCellDataSuccess;

  constructor(public payload: InputData) {}
}

export class UpdateInputRowCellDataFail extends FailedAction {
  readonly type = ActionTypes.UpdateInputRowCellDataFail;

  constructor(public id: number, httpErrorResponse: HttpErrorResponse) {
    super(httpErrorResponse);
  }
}

export class GetTestExpectedRows {
  readonly type = ActionTypes.GetTestExpectedRows;

  constructor(public id: number) {}
}

export class GetTestExpectedRowsSuccess {
  readonly type = ActionTypes.GetTestExpectedRowsSuccess;

  constructor(public outputRows: OutputData[]) {}
}

export class GetTestExpectedRowsFail extends FailedAction {
  readonly type = ActionTypes.GetTestExpectedRowsFail;
}

export class CreateExpectedDataRow {
  readonly type = ActionTypes.CreateExpectedDataRow;

  constructor(
    public request: CreateExpectedDataRowRequest,
    public id: number
  ) {}
}

export class CreateExpectedDataRowFail extends FailedAction {
  readonly type = ActionTypes.CreateExpectedDataRowFail;

  constructor(public id: number, httpErrorResponse: HttpErrorResponse) {
    super(httpErrorResponse);
  }
}

export class CreateExpectedDataRowSuccess {
  readonly type = ActionTypes.CreateExpectedDataRowSuccess;

  constructor(public id: number) {}
}

export class DeleteExpectedDataRow {
  readonly type = ActionTypes.DeleteExpectedDataRow;

  constructor(public id: number, public rowId: number) {}
}

export class DeleteExpectedDataRowFail extends FailedAction {
  readonly type = ActionTypes.DeleteExpectedDataRowFail;

  constructor(public id: number, httpErrorResponse: HttpErrorResponse) {
    super(httpErrorResponse);
  }
}

export class DeleteExpectedDataRowSuccess {
  readonly type = ActionTypes.DeleteExpectedDataRowSuccess;

  constructor(public id: number) {}
}

export class UpdateExpectedRowCellData {
  readonly type = ActionTypes.UpdateExpectedRowCellData;

  constructor(
    public id: number,
    public rowId: number,
    public rowCellDataId: number,
    public request: UpdateRowCellDataRequest
  ) {}
}

export class UpdateExpectedRowCellDataSuccess {
  readonly type = ActionTypes.UpdateExpectedRowCellDataSuccess;

  constructor(public payload: InputData) {}
}

export class UpdateExpectedRowCellDataFail extends FailedAction {
  readonly type = ActionTypes.UpdateExpectedRowCellDataFail;

  constructor(public id: number, httpErrorResponse: HttpErrorResponse) {
    super(httpErrorResponse);
  }
}

export type Actions =
  | GetTestInputRows
  | GetTestInputRowsSuccess
  | GetTestInputRowsFail
  | CreateInputDataRow
  | CreateInputDataRowFail
  | CreateInputDataRowSuccess
  | DeleteInputDataRow
  | DeleteInputDataRowFail
  | DeleteInputDataRowSuccess
  | UpdateInputRowCellData
  | UpdateInputRowCellDataFail
  | UpdateInputRowCellDataSuccess
  | GetTestExpectedRows
  | GetTestExpectedRowsSuccess
  | GetTestExpectedRowsFail
  | CreateExpectedDataRow
  | CreateExpectedDataRowFail
  | CreateExpectedDataRowSuccess
  | DeleteExpectedDataRow
  | DeleteExpectedDataRowFail
  | DeleteExpectedDataRowSuccess
  | UpdateExpectedRowCellData
  | UpdateExpectedRowCellDataFail
  | UpdateExpectedRowCellDataSuccess
  ;
