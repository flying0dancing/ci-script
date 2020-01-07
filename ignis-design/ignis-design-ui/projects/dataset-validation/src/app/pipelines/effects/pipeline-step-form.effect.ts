import { HttpErrorResponse } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Actions, Effect, ofType } from '@ngrx/effects';
import { of } from 'rxjs';
import { catchError, mergeMap, switchMap } from 'rxjs/operators';
import {
  Delete,
  DeleteFail,
  DeleteSuccess,
  PipelineStepFormActionTypes,
  Post,
  PostFailed,
  PostSuccessful,
  Update,
  UpdateFailed,
  UpdateSuccessful
} from '../actions/pipeline-step-form.actions';
import { GetAll } from '../actions/pipelines.actions';
import { PipelineStepsRepository } from '../services/pipeline-steps.repository';

@Injectable()
export class PipelineStepFormEffects {
  constructor(
    private actions$: Actions,
    private stepsRepository: PipelineStepsRepository
  ) {}

  @Effect()
  create$ = this.actions$.pipe(
    ofType(PipelineStepFormActionTypes.Post),
    switchMap((postAction: Post) => {
      return this.stepsRepository
        .save(postAction.pipelineId, postAction.step)
        .pipe(
          mergeMap(() => [
            new PostSuccessful(postAction.pipelineId),
            new GetAll()
          ]),
          catchError((httpError: HttpErrorResponse) =>
            of(new PostFailed(httpError.error))
          )
        );
    })
  );

  @Effect()
  update$ = this.actions$.pipe(
    ofType(PipelineStepFormActionTypes.Update),
    switchMap((updateAction: Update) => {
      return this.stepsRepository
        .update(
          updateAction.pipelineId,
          updateAction.pipelineStepId,
          updateAction.step
        )
        .pipe(
          mergeMap(() => [
            new UpdateSuccessful(updateAction.pipelineId),
            new GetAll()
          ]),
          catchError((httpError: HttpErrorResponse) =>
            of(new UpdateFailed(httpError.error))
          )
        );
    })
  );

  @Effect()
  delete$ = this.actions$.pipe(
    ofType(PipelineStepFormActionTypes.Delete),
    switchMap((deleteAction: Delete) =>
      this.stepsRepository
        .delete(deleteAction.pipelineId, deleteAction.id)
        .pipe(
          mergeMap(() => [new DeleteSuccess(), new GetAll()]),
          catchError((httpError: HttpErrorResponse) =>
            of(new DeleteFail(httpError))
          )
        )
    )
  );
}
