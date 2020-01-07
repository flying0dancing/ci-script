import { HttpErrorResponse } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Actions, Effect, ofType } from '@ngrx/effects';
import { of } from 'rxjs';
import { catchError, map, mergeMap, switchMap } from 'rxjs/operators';
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
  PipelineActionTypes,
  Update,
  UpdateFail,
  UpdateSuccess
} from '../actions/pipelines.actions';
import { PipelinesRepository } from '../services/pipelines.repository';

@Injectable()
export class PipelinesEffects {
  constructor(
    private actions$: Actions,
    private pipelinesRepository: PipelinesRepository
  ) {}

  @Effect()
  create$ = this.actions$.pipe(
    ofType(PipelineActionTypes.Create),
    switchMap((payload: Create) =>
      this.pipelinesRepository.create(payload.request).pipe(
        mergeMap(() => [new CreateSuccess(), new GetAll()]),
        catchError((httpResponse: HttpErrorResponse) =>
          of(new CreateFail(httpResponse))
        )
      )
    )
  );

  @Effect()
  getOne$ = this.actions$.pipe(
    ofType<GetOne>(PipelineActionTypes.GetOne),
    mergeMap(action =>
      this.pipelinesRepository.getOne(action.id).pipe(
        map(pipeline => new GetOneSuccess(pipeline)),
        catchError((httpResponse: HttpErrorResponse) =>
          of(new GetOneFail(httpResponse))
        )
      )
    )
  );

  @Effect()
  getAll$ = this.actions$.pipe(
    ofType(PipelineActionTypes.GetAll),
    switchMap(() =>
      this.pipelinesRepository.getAll().pipe(
        map(pipelines => new GetAllSuccess(pipelines)),
        catchError((httpResponse: HttpErrorResponse) =>
          of(new GetAllFail(httpResponse))
        )
      )
    )
  );

  @Effect()
  update$ = this.actions$.pipe(
    ofType(PipelineActionTypes.Update),
    switchMap((payload: Update) =>
      this.pipelinesRepository.update(payload.id, payload.request).pipe(
        mergeMap(() => [new UpdateSuccess(), new GetAll()]),
        catchError((httpResponse: HttpErrorResponse) =>
          of(new UpdateFail(httpResponse))
        )
      )
    )
  );

  @Effect()
  delete$ = this.actions$.pipe(
    ofType(PipelineActionTypes.Delete),
    switchMap((payload: Delete) =>
      this.pipelinesRepository.delete(payload.id).pipe(
        mergeMap(id => [new DeleteSuccess(id), new GetAll()]),
        catchError((httpResponse: HttpErrorResponse) =>
          of(new DeleteFail(httpResponse))
        )
      )
    )
  );
}
