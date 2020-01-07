import { HttpErrorResponse } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Actions, Effect, ofType } from '@ngrx/effects';
import { of } from 'rxjs';
import { catchError, filter, map, mergeMap, takeUntil } from 'rxjs/operators';
import * as rowActions from '../actions/pipeline-step-test-rows.actions';
import * as actions from '../actions/pipeline-step-test.actions';
import { PipelineStepTestsRepository } from '../services/pipeline-step-tests.repository';

@Injectable()
export class PipelineStepTestEffects {
  constructor(
    private actions$: Actions,
    private pipelineStepTestsRepository: PipelineStepTestsRepository
  ) {}

  @Effect()
  getOne$ = this.actions$.pipe(
    ofType<actions.GetOne>(actions.ActionTypes.GetOne),
    mergeMap(action =>
      this.pipelineStepTestsRepository.getOne(action.id).pipe(
        takeUntil(this.cancelGetOneAction(action)),
        map(pipelineStepTest => new actions.GetOneSuccess(pipelineStepTest)),
        catchError((httpResponse: HttpErrorResponse) =>
          of(new actions.GetOneFail(action.id, httpResponse))
        )
      )
    )
  );

  @Effect()
  update$ = this.actions$.pipe(
    ofType(actions.ActionTypes.Update),
    mergeMap((updateAction: actions.Update) =>
      this.pipelineStepTestsRepository
        .update(updateAction.id, updateAction.request)
        .pipe(
          takeUntil(this.cancelUpdateAction(updateAction)),
          map(pipelineStepTest => new actions.UpdateSuccess(pipelineStepTest)),
          catchError((httpError: HttpErrorResponse) =>
            of(new actions.UpdateFail(updateAction.id, httpError))
          )
        )
    )
  );

  @Effect()
  delete$ = this.actions$.pipe(
    ofType<actions.Delete>(actions.ActionTypes.Delete),
    mergeMap(action =>
      this.pipelineStepTestsRepository.delete(action.id).pipe(
        takeUntil(this.cancelDeleteAction(action)),
        map(pipelineStepTest => new actions.DeleteSuccess(pipelineStepTest)),
        catchError((httpResponse: HttpErrorResponse) =>
          of(new actions.DeleteFail(action.id, httpResponse))
        )
      )
    )
  );

  @Effect()
  run$ = this.actions$.pipe(
    ofType(actions.ActionTypes.Run),
    mergeMap((action: actions.Run) =>
      this.pipelineStepTestsRepository.run(action.id).pipe(
        takeUntil(this.cancelRunAction(action)),
        mergeMap(_ => [
          new actions.RunSuccess(action.id),
          new rowActions.GetTestExpectedRows(action.id)
        ]),
        catchError((httpError: HttpErrorResponse) =>
          of(new actions.RunFail(action.id, httpError))
        )
      )
    )
  );

  private cancelGetOneAction = (action: actions.GetOne) =>
    this.actions$.pipe(
      ofType(actions.ActionTypes.GetOne),
      filter((latestAction: actions.GetOne) => action.id === latestAction.id)
    );

  private cancelUpdateAction = (action: actions.Update) =>
    this.actions$.pipe(
      ofType(actions.ActionTypes.Update),
      filter((latestAction: actions.Update) => action.id === latestAction.id)
    );

  private cancelDeleteAction = (action: actions.Delete) =>
    this.actions$.pipe(
      ofType(actions.ActionTypes.Delete),
      filter((latestAction: actions.Delete) => action.id === latestAction.id)
    );

  private cancelRunAction = (action: actions.Run) =>
    this.actions$.pipe(
      ofType(actions.ActionTypes.Run),
      filter((latestAction: actions.Run) => action.id === latestAction.id)
    );
}
