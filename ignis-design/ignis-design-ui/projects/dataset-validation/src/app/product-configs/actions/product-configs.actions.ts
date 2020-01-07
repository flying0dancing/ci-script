import { Action } from '@ngrx/store';
import { GridDeleteAction } from '../../core/grid/interfaces/delete-params.interface';
import { FailedAction } from '../../core/utilities/interfaces/errors.interface';

import { GetAllIdentifiableSuccessAction } from '../../core/utilities/interfaces/get-all-identifiable-action.interface';
import { GetOneIdentifiableSuccessAction } from '../../core/utilities/interfaces/get-one-identifiable-action.interface';
import { CreateProductConfigRequest } from '../interfaces/create-product-request.interface';
import { ProductConfig } from '../interfaces/product-config.interface';
import { PipelineTask, ProductConfigTaskList } from '../interfaces/product-validation.interface';
import { UpdateProductConfigRequest } from '../interfaces/update-product-request.interface';

export enum ProductActionTypes {
  GetOne = '[ProductConfig] Get One',
  GetOneSuccess = '[ProductConfig] Get One Success',
  GetOneFail = '[ProductConfig] Get One Fail',

  GetAll = '[ProductConfig] Get All',
  GetAllSuccessful = '[ProductConfig] Get All Success',
  GetAllFailed = '[ProductConfig] Get All Fail',

  Create = '[ProductConfig] Create',
  CreateSuccessful = '[ProductConfig] Create Success',
  CreateFailed = '[ProductConfig] Create Fail',

  Update = '[ProductConfig] Update',
  UpdateSuccessful = '[ProductConfig] Update Success',
  UpdateFailed = '[ProductConfig] Update Fail',

  Delete = '[ProductConfig] Delete',
  DeleteSuccessful = '[ProductConfig] Delete Success',
  DeleteFailed = '[ProductConfig] Delete Fail',

  Validate = '[ProductConfig] Validate',
  ValidateCancel = '[ProductConfig] Validate Cancel',
  ValidateFail = '[ProductConfig] Validate Fail',
  ValidatePipelineTaskUpdate = '[ProductConfig] Validate Pipeline Task Update',
  ValidateSuccess = '[ProductConfig] Validate Success',

  GetTaskList = '[ProductConfig] Get Task List',
  GetTaskListSuccess = '[ProductConfig] Get Task List Success',
  GetTaskListFailed = '[ProductConfig] Get Task List Fail'
}

export class GetOne implements Action {
  readonly type = ProductActionTypes.GetOne;

  constructor(public id: number) {}
}

export class GetOneFail extends FailedAction {
  readonly type = ProductActionTypes.GetOneFail;
}

export class GetOneSuccess implements GetOneIdentifiableSuccessAction {
  readonly type = ProductActionTypes.GetOneSuccess;

  constructor(public payload: ProductConfig) {}

  id = this.payload.id;
}

export class GetAll implements Action {
  readonly type = ProductActionTypes.GetAll;
}

export class GetAllFail extends FailedAction {
  readonly type = ProductActionTypes.GetAllFailed;
}

export class GetAllSuccess implements GetAllIdentifiableSuccessAction {
  readonly type = ProductActionTypes.GetAllSuccessful;

  constructor(public payload: ProductConfig[]) {}

  get ids(): number[] {
    return this.payload.map(productConfig => productConfig.id);
  }
}

export class Create implements Action {
  readonly type = ProductActionTypes.Create;

  constructor(public request: CreateProductConfigRequest) {}
}

export class CreateFail extends FailedAction {
  readonly type = ProductActionTypes.CreateFailed;
}

export class CreateSuccess implements Action {
  readonly type = ProductActionTypes.CreateSuccessful;

  constructor(public id: number) {}
}

export class Update implements Action {
  readonly type = ProductActionTypes.Update;

  constructor(public id: number, public request: UpdateProductConfigRequest) {}
}

export class UpdateFail extends FailedAction {
  readonly type = ProductActionTypes.UpdateFailed;
}

export class UpdateSuccess implements Action {
  readonly type = ProductActionTypes.UpdateSuccessful;
}

export class Delete implements GridDeleteAction {
  readonly type = ProductActionTypes.Delete;

  constructor(public id: number) {}
}

export class DeleteFail extends FailedAction {
  readonly type = ProductActionTypes.DeleteFailed;
}

export class DeleteSuccess implements Action {
  readonly type = ProductActionTypes.DeleteSuccessful;

  constructor(public id: number) {}
}

export class Validate implements Action {
  readonly type = ProductActionTypes.Validate;

  constructor(public id: number) {}
}

export class ValidateCancel implements Action {
  readonly type = ProductActionTypes.ValidateCancel;

  constructor(public id: number) {}
}

export class ValidateFail implements Action {
  readonly type = ProductActionTypes.ValidateFail;

  constructor(public message: string) {}
}

export class ValidatePipelineTaskUpdate implements Action {
  readonly type = ProductActionTypes.ValidatePipelineTaskUpdate;

  constructor(public productId: number, public event: PipelineTask) {}
}

export class ValidateSuccess implements Action {
  readonly type = ProductActionTypes.ValidateSuccess;

  constructor(public productId: number) {}
}

export class GetTaskList implements Action {
  readonly type = ProductActionTypes.GetTaskList;
  constructor(public id: number) {}
}

export class GetTaskListSuccess implements Action {
  readonly type = ProductActionTypes.GetTaskListSuccess;

  constructor(public taskList: ProductConfigTaskList) {}
}

export class GetTaskListFailed extends FailedAction {
  readonly type = ProductActionTypes.GetTaskListFailed;
}

export type ProductActions =
  | GetOne
  | GetOneFail
  | GetOneSuccess
  | GetAll
  | GetAllFail
  | GetAllSuccess
  | Create
  | CreateSuccess
  | CreateFail
  | Update
  | UpdateSuccess
  | UpdateFail
  | Delete
  | DeleteSuccess
  | DeleteFail
  | Validate
  | ValidateCancel
  | ValidateFail
  | ValidatePipelineTaskUpdate
  | ValidateSuccess
  | GetTaskList
  | GetTaskList
  | GetTaskListSuccess
  | GetTaskListFailed;
