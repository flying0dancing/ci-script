import { HttpErrorResponse } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Actions, Effect, ofType } from '@ngrx/effects';
import { of } from 'rxjs';
import { catchError, map, switchMap, mergeMap } from 'rxjs/operators';
import * as actions from '../actions/pipeline-step-tests.actions';
import { PipelineStepTestsRepository } from '../services/pipeline-step-tests.repository';

@Injectable()
export class PipelineStepTestsEffects {
  constructor(
    private actions$: Actions,
    private pipelineStepTestsRepository: PipelineStepTestsRepository
  ) {}

  @Effect()
  getAll$ = this.actions$.pipe(
    ofType<actions.GetAll>(actions.ActionTypes.GetAll),
    switchMap(action =>
      this.pipelineStepTestsRepository.getAll(action.pipelineId).pipe(
        map(pipelineStepTests => new actions.GetAllSuccess(pipelineStepTests)),
        catchError((httpResponse: HttpErrorResponse) =>
          of(new actions.GetAllFail(httpResponse))
        )
      )
    )
  );

  @Effect()
  create$ = this.actions$.pipe(
    ofType(actions.ActionTypes.Create),
    switchMap((postAction: actions.Create) =>
      this.pipelineStepTestsRepository.create(postAction.request).pipe(
        mergeMap(_ => [
          new actions.CreateSuccess(),
          new actions.GetAll(postAction.pipelineId)
        ]),
        catchError((httpError: HttpErrorResponse) =>
          of(new actions.CreateFail(httpError))
        )
      )
    )
  );
}
