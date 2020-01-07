import { HttpErrorResponse } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Actions, Effect, ofType } from '@ngrx/effects';
import { of } from 'rxjs';
import { catchError, map, mergeMap, switchMap, takeUntil } from 'rxjs/operators';
import {
  Create,
  CreateFail,
  CreateSuccess,
  Delete,
  DeleteFail,
  DeleteSuccess,
  GetAll,
  GetAllFail,
  GetAllSuccess,
  GetOne,
  GetOneFail,
  GetOneSuccess,
  GetTaskList,
  GetTaskListFailed,
  GetTaskListSuccess,
  ProductActionTypes,
  Update,
  UpdateFail,
  UpdateSuccess,
  Validate,
  ValidateFail,
  ValidatePipelineTaskUpdate,
  ValidateSuccess
} from '../actions/product-configs.actions';
import { TaskType, ValidationEvent } from '../interfaces/product-validation.interface';

import { ProductConfigsRepository } from '../services/product-configs.repository';

@Injectable()
export class ProductConfigsEffects {

  @Effect()
  getOne$ = this.actions$.pipe(
    ofType<GetOne>(ProductActionTypes.GetOne),
    mergeMap((action) =>
      this.productConfigsRepository.getOne(action.id).pipe(
        map(productConfig => new GetOneSuccess(productConfig)),
        catchError((httpResponse: HttpErrorResponse) =>
          of(new GetOneFail(httpResponse))
        )
      )
    )
  );

  @Effect()
  getAll$ = this.actions$.pipe(
    ofType(ProductActionTypes.GetAll),
    switchMap(() =>
      this.productConfigsRepository.getAll().pipe(
        map(productConfigs => new GetAllSuccess(productConfigs)),
        catchError((httpResponse: HttpErrorResponse) =>
          of(new GetAllFail(httpResponse))
        )
      )
    )
  );

  @Effect()
  create$ = this.actions$.pipe(
    ofType(ProductActionTypes.Create),
    switchMap((payload: Create) =>
      this.productConfigsRepository.create(payload.request).pipe(
        mergeMap(id => [new CreateSuccess(id), new GetAll()]),
        catchError((httpResponse: HttpErrorResponse) =>
          of(new CreateFail(httpResponse))
        )
      )
    )
  );

  @Effect()
  update$ = this.actions$.pipe(
    ofType(ProductActionTypes.Update),
    switchMap((payload: Update) =>
      this.productConfigsRepository
        .updateVersion(payload.id, payload.request)
        .pipe(
          mergeMap(() => [new UpdateSuccess(), new GetAll()]),
          catchError((httpResponse: HttpErrorResponse) =>
            of(new UpdateFail(httpResponse))
          )
        )
    )
  );

  @Effect()
  delete$ = this.actions$.pipe(
    ofType(ProductActionTypes.Delete),
    switchMap((payload: Delete) =>
      this.productConfigsRepository.delete(payload.id).pipe(
        mergeMap(id => [new DeleteSuccess(id), new GetAll()]),
        catchError((httpResponse: HttpErrorResponse) =>
          of(new DeleteFail(httpResponse))
        )
      )
    )
  );

  @Effect()
  validate$ = this.actions$.pipe(
    ofType(ProductActionTypes.Validate),
    switchMap((payload: Validate) =>
      this.productConfigsRepository.validate(payload.id).pipe(
        takeUntil(this.cancelValidateActions$),
        map((event: ValidationEvent) => {
          switch (event.type) {
            case TaskType.Pipeline:
              return new ValidatePipelineTaskUpdate(payload.id, event);
            case TaskType.Complete:
              return new ValidateSuccess(payload.id);
            default:
              return new ValidateFail('Event type not supported ' + event);
          }
        })
      )
    )
  );

  @Effect()
  getTaskList$ = this.actions$.pipe(
    ofType(ProductActionTypes.GetTaskList),
    switchMap((action: GetTaskList) =>
      this.productConfigsRepository.getTaskList(action.id).pipe(
        map(taskLists => new GetTaskListSuccess(taskLists)),
        catchError((httpResponse: HttpErrorResponse) =>
          of(new GetTaskListFailed(httpResponse))
        )
      )
    )
  );

  private cancelValidateActions$ = this.actions$.pipe(
    ofType(ProductActionTypes.ValidateCancel)
  );

  constructor(
    private actions$: Actions,
    private productConfigsRepository: ProductConfigsRepository
  ) {}
}
