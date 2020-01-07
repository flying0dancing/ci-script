import { Injectable } from '@angular/core';
import { Actions, Effect, ofType } from '@ngrx/effects';
import { of } from 'rxjs';
import { catchError, map, mergeMap, switchMap } from 'rxjs/operators';
import {
  COPY,
  Copy,
  CopyFail,
  CopySuccess,
  Create,
  CREATE,
  CREATE_NEW_VERSION,
  CreateFail,
  CreateNewVersion,
  CreateNewVersionFail,
  CreateNewVersionSuccess,
  CreateSuccess,
  Delete,
  DELETE,
  DeleteFail,
  DeleteSuccess,
  Get,
  GET_ONE,
  GetOne,
  GetOneFail,
  GetOneSuccess,
  UPDATE_ONE,
  UPDATE_REQUEST,
  UpdateOne,
  UpdateOneFail,
  UpdateOneSuccess,
  UpdateRequest,
  UpdateRequestFail,
  UpdateRequestSuccess
} from '..';
import * as ProductActions from '../../product-configs/actions/product-configs.actions';

import { SchemasRepository } from '../services/schemas.repository';

@Injectable()
export class SchemasEffects {
  @Effect()
  create$ = this.actions$.pipe(
    ofType<Create>(CREATE),
    switchMap(createSchema =>
      this.schemasRepository
        .create(createSchema.productId, createSchema.request)
        .pipe(
          mergeMap(response => [
            new CreateSuccess(response.table),
            new Get(),
            new ProductActions.GetAll()
          ]),
          catchError(() => of(new CreateFail()))
        )
    )
  );

  @Effect()
  createNewVersion$ = this.actions$.pipe(
    ofType<CreateNewVersion>(CREATE_NEW_VERSION),
    switchMap(createNewSchemaVersion =>
      this.schemasRepository
        .createNewVersions(
          createNewSchemaVersion.productId,
          createNewSchemaVersion.schemaId,
          createNewSchemaVersion.startDate
        )
        .pipe(
          mergeMap(response => [
            new CreateNewVersionSuccess(response.table),
            new Get(),
            new ProductActions.GetAll()
          ]),
          catchError(() => of(new CreateNewVersionFail()))
        )
    )
  );

  @Effect()
  copySchema$ = this.actions$.pipe(
    ofType<Copy>(COPY),
    switchMap((copyAction) =>
      this.schemasRepository.copy(copyAction.productId, copyAction.schemaId, copyAction.request)
        .pipe(
          mergeMap(response => [new CopySuccess(response.table), new Get(), new ProductActions.GetAll()]),
          catchError(() => of(new CopyFail()))))
  );

  @Effect()
  getOne$ = this.actions$.pipe(
    ofType<GetOne>(GET_ONE),
    switchMap(payload =>
      this.schemasRepository.getById(payload.productId, payload.id).pipe(
        map(response => new GetOneSuccess(response)),
        catchError(() => of(new GetOneFail()))
      )
    )
  );

  @Effect()
  delete$ = this.actions$.pipe(
    ofType<Delete>(DELETE),
    switchMap(payload =>
      this.schemasRepository.delete(payload.productId, payload.id).pipe(
        mergeMap(id => [
          new DeleteSuccess(id),
          new Get(),
          new ProductActions.GetAll()
        ]),
        catchError(() => of(new DeleteFail()))
      )
    )
  );

  @Effect()
  updateOne$ = this.actions$.pipe(
    ofType<UpdateOne>(UPDATE_ONE),
    switchMap(payload =>
      this.schemasRepository.getById(payload.productId, payload.id).pipe(
        mergeMap(response => [
          new UpdateOneSuccess(response),
          new ProductActions.GetAll()
        ]),
        catchError(() => of(new UpdateOneFail()))
      )
    )
  );

  @Effect()
  updateRequest$ = this.actions$.pipe(
    ofType<UpdateRequest>(UPDATE_REQUEST),
    switchMap(payload =>
      this.schemasRepository
        .updateSchemaDetails(
          payload.productId,
          payload.schemaId,
          payload.request
        )
        .pipe(
          mergeMap(() => [
            new UpdateRequestSuccess(),
            new GetOne(payload.productId, payload.schemaId)
          ]),
          catchError(() => of(new UpdateRequestFail()))
        )
    )
  );

  constructor(
    private actions$: Actions,
    private schemasRepository: SchemasRepository
  ) {}
}
