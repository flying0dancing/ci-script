import { HttpErrorResponse } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Actions, Effect, ofType } from '@ngrx/effects';
import { of } from 'rxjs';
import { catchError, filter, map, mergeMap, switchMap, takeUntil } from 'rxjs/operators';
import * as actions from '../actions/pipeline-step-test-rows.actions';
import { PipelineStepTestsRepository } from '../services/pipeline-step-tests.repository';

@Injectable()
export class PipelineStepTestRowsEffects {
  constructor(
    private actions$: Actions,
    private pipelineStepTestsRepository: PipelineStepTestsRepository
  ) {}

  @Effect()
  getTestInputRows$ = this.actions$.pipe(
    ofType<actions.GetTestInputRows>(actions.ActionTypes.GetTestInputRows),
    mergeMap(action =>
      this.pipelineStepTestsRepository.getTestInputRows(action.id).pipe(
        takeUntil(this.cancelGetTestInputRowsAction(action)),
        map(inputRows => new actions.GetTestInputRowsSuccess(inputRows)),
        catchError((httpResponse: HttpErrorResponse) =>
          of(new actions.GetTestInputRowsFail(httpResponse))
        )
      )
    )
  );

  @Effect()
  createInputDataRow$ = this.actions$.pipe(
    ofType(actions.ActionTypes.CreateInputDataRow),
    mergeMap((createAction: actions.CreateInputDataRow) =>
      this.pipelineStepTestsRepository
        .createInputDataRow(createAction.id, createAction.request)
        .pipe(
          takeUntil(this.cancelCreateInputDataRowAction(createAction)),
          mergeMap(_ => [
            new actions.CreateInputDataRowSuccess(createAction.id),
            new actions.GetTestInputRows(createAction.id)
          ]),
          catchError((httpError: HttpErrorResponse) =>
            of(new actions.CreateInputDataRowFail(createAction.id, httpError))
          )
        )
    )
  );

  @Effect()
  deleteInputDataRow$ = this.actions$.pipe(
    ofType(actions.ActionTypes.DeleteInputDataRow),
    mergeMap((deleteAction: actions.DeleteInputDataRow) =>
      this.pipelineStepTestsRepository
        .deleteInputDataRow(deleteAction.id, deleteAction.rowId)
        .pipe(
          takeUntil(this.cancelDeleteInputDataRowAction(deleteAction)),
          mergeMap(_ => [
            new actions.DeleteInputDataRowSuccess(deleteAction.id),
            new actions.GetTestInputRows(deleteAction.id)
          ]),
          catchError((httpError: HttpErrorResponse) =>
            of(new actions.DeleteInputDataRowFail(deleteAction.id, httpError))
          )
        )
    )
  );

  @Effect()
  updateRowCellData$ = this.actions$.pipe(
    ofType(actions.ActionTypes.UpdateInputRowCellData),
    mergeMap((updateAction: actions.UpdateInputRowCellData) =>
      this.pipelineStepTestsRepository
        .updateRowCellData(
          updateAction.id,
          updateAction.rowId,
          updateAction.rowCellDataId,
          updateAction.request
        )
        .pipe(
          takeUntil(this.cancelUpdateRowCellDataAction(updateAction)),
          mergeMap(row => [
            new actions.UpdateInputRowCellDataSuccess(row),
            new actions.GetTestInputRows(updateAction.id)
          ]),
          catchError((httpError: HttpErrorResponse) =>
            of(new actions.UpdateInputRowCellDataFail(updateAction.id, httpError))
          )
        )
    )
  );

  @Effect()
  getTestExpectedRows$ = this.actions$.pipe(
    ofType<actions.GetTestExpectedRows>(actions.ActionTypes.GetTestExpectedRows),
    mergeMap(action =>
      this.pipelineStepTestsRepository.getTestOutputRows(action.id).pipe(
        takeUntil(this.cancelGetTestExpectedRowsAction(action)),
        map(inputRows => new actions.GetTestExpectedRowsSuccess(inputRows)),
        catchError((httpResponse: HttpErrorResponse) =>
          of(new actions.GetTestExpectedRowsFail(httpResponse))
        )
      )
    )
  );

  @Effect()
  createExpectedDataRow$ = this.actions$.pipe(
    ofType(actions.ActionTypes.CreateExpectedDataRow),
    switchMap((createAction: actions.CreateExpectedDataRow) =>
      this.pipelineStepTestsRepository
        .createExpectedDataRow(createAction.id, createAction.request)
        .pipe(
          takeUntil(this.cancelCreateExpectedDataRowAction(createAction)),
          mergeMap(_ => [
            new actions.CreateExpectedDataRowSuccess(createAction.id),
            new actions.GetTestExpectedRows(createAction.id)
          ]),
          catchError((httpError: HttpErrorResponse) =>
            of(
              new actions.CreateExpectedDataRowFail(createAction.id, httpError)
            )
          )
        )
    )
  );

  @Effect()
  deleteExpectedDataRow$ = this.actions$.pipe(
    ofType(actions.ActionTypes.DeleteExpectedDataRow),
    mergeMap((deleteAction: actions.DeleteExpectedDataRow) =>
      this.pipelineStepTestsRepository
        .deleteExpectedDataRow(deleteAction.id, deleteAction.rowId)
        .pipe(
          takeUntil(this.cancelDeleteExpectedDataRowAction(deleteAction)),
          mergeMap(_ => [
            new actions.DeleteExpectedDataRowSuccess(deleteAction.id),
            new actions.GetTestExpectedRows(deleteAction.id)
          ]),
          catchError((httpError: HttpErrorResponse) =>
            of(
              new actions.DeleteExpectedDataRowFail(deleteAction.id, httpError)
            )
          )
        )
    )
  );

  @Effect()
  updateExpectedRowCellData$ = this.actions$.pipe(
    ofType(actions.ActionTypes.UpdateExpectedRowCellData),
    mergeMap((updateAction: actions.UpdateExpectedRowCellData) =>
      this.pipelineStepTestsRepository
        .updateRowCellData(
          updateAction.id,
          updateAction.rowId,
          updateAction.rowCellDataId,
          updateAction.request
        )
        .pipe(
          takeUntil(this.cancelUpdateExpectedRowCellDataAction(updateAction)),
          mergeMap(row => [
            new actions.UpdateExpectedRowCellDataSuccess(row),
            new actions.GetTestExpectedRows(updateAction.id)
          ]),
          catchError((httpError: HttpErrorResponse) =>
            of(new actions.UpdateExpectedRowCellDataFail(updateAction.id, httpError))
          )
        )
    )
  );

  private cancelGetTestInputRowsAction = (action: actions.GetTestInputRows) =>
    this.actions$.pipe(
      ofType(actions.ActionTypes.GetTestInputRows),
      filter((latestAction: actions.GetTestInputRows) => action.id === latestAction.id)
    );

  private cancelCreateInputDataRowAction = (
    action: actions.CreateInputDataRow
  ) =>
    this.actions$.pipe(
      ofType(actions.ActionTypes.CreateInputDataRow),
      filter(
        (latestAction: actions.CreateInputDataRow) =>
          action.id === latestAction.id &&
          action.request.schemaId === action.request.schemaId
      )
    );

  private cancelDeleteInputDataRowAction = (
    action: actions.DeleteInputDataRow
  ) =>
    this.actions$.pipe(
      ofType(actions.ActionTypes.DeleteInputDataRow),
      filter(
        (latestAction: actions.DeleteInputDataRow) =>
          action.id === latestAction.id && action.rowId === latestAction.rowId
      )
    );

  private cancelUpdateRowCellDataAction = (action: actions.UpdateInputRowCellData) =>
    this.actions$.pipe(
      ofType(actions.ActionTypes.UpdateInputRowCellData),
      filter(
        (latestAction: actions.UpdateInputRowCellData) =>
          action.rowId === latestAction.rowId &&
          action.rowCellDataId === latestAction.rowCellDataId
      )
    );

  private cancelGetTestExpectedRowsAction = (action: actions.GetTestExpectedRows) =>
    this.actions$.pipe(
      ofType(actions.ActionTypes.GetTestExpectedRows),
      filter((latestAction: actions.GetTestExpectedRows) => action.id === latestAction.id)
    );

  private cancelCreateExpectedDataRowAction = (
    action: actions.CreateExpectedDataRow
  ) =>
    this.actions$.pipe(
      ofType(actions.ActionTypes.CreateExpectedDataRow),
      filter(
        (latestAction: actions.CreateExpectedDataRow) =>
          action.id === latestAction.id &&
          action.request.schemaId === action.request.schemaId
      )
    );

  private cancelDeleteExpectedDataRowAction = (
    action: actions.DeleteExpectedDataRow
  ) =>
    this.actions$.pipe(
      ofType(actions.ActionTypes.DeleteExpectedDataRow),
      filter(
        (latestAction: actions.DeleteExpectedDataRow) =>
          action.id === latestAction.id && action.rowId === latestAction.rowId
      )
    );

  private cancelUpdateExpectedRowCellDataAction = (action: actions.UpdateExpectedRowCellData) =>
    this.actions$.pipe(
      ofType(actions.ActionTypes.UpdateExpectedRowCellData),
      filter(
        (latestAction: actions.UpdateExpectedRowCellData) =>
          action.rowId === latestAction.rowId &&
          action.rowCellDataId === latestAction.rowCellDataId
      )
    );
}
