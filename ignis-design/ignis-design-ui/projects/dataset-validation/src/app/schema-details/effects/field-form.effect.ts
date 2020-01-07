import { HttpErrorResponse } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Actions, Effect, ofType } from '@ngrx/effects';
import { of } from 'rxjs';
import { catchError, map, mergeMap, switchMap } from 'rxjs/operators';
import * as schemaActions from '../../schemas';
import { UpdateOne } from '../../schemas';
import {
  Delete,
  DeleteFail,
  DeleteSuccess,
  Edit,
  EditFailed,
  EditSuccessful,
  FieldFormActionTypes,
  Post,
  PostFailed,
  PostSuccessful
} from '../actions/field-form.actions';
import { FieldsRepository } from '../service/fields.repository';

@Injectable()
export class FieldFormEffects {
  constructor(
    private actions$: Actions,
    private fieldsRepository: FieldsRepository
  ) {}

  @Effect()
  post$ = this.actions$.pipe(
    ofType(FieldFormActionTypes.Post),
    switchMap((postAction: Post) => {
      return this.fieldsRepository
        .save(postAction.productId, postAction.schemaId, postAction.field)
        .pipe(
          map(
            () => new PostSuccessful(postAction.productId, postAction.schemaId)
          ),
          catchError((httpError: HttpErrorResponse) =>
            of(new PostFailed(httpError))
          )
        );
    })
  );

  @Effect()
  postSuccessful$ = this.actions$.pipe(
    ofType(FieldFormActionTypes.PostSuccessful),
    map(
      (postAction: PostSuccessful) =>
        new schemaActions.UpdateOne(postAction.productId, postAction.schemaId)
    )
  );
  @Effect()
  edit$ = this.actions$.pipe(
    ofType(FieldFormActionTypes.Edit),
    switchMap((editAction: Edit) => {
      return this.fieldsRepository
        .edit(editAction.productId, editAction.schemaId, editAction.field)
        .pipe(
          map(
            () => new EditSuccessful(editAction.productId, editAction.schemaId)
          ),
          catchError((httpError: HttpErrorResponse) =>
            of(new EditFailed(httpError))
          )
        );
    })
  );

  @Effect()
  editSuccessful$ = this.actions$.pipe(
    ofType(FieldFormActionTypes.EditSuccessful),
    map(
      (editAction: EditSuccessful) =>
        new schemaActions.UpdateOne(editAction.productId, editAction.schemaId)
    )
  );

  @Effect()
  delete$ = this.actions$.pipe(
    ofType(FieldFormActionTypes.Delete),
    switchMap((event: Delete) =>
      this.fieldsRepository
        .delete(event.productId, event.schemaId, event.id)
        .pipe(
          mergeMap(() => [
            new DeleteSuccess(),
            new UpdateOne(event.productId, event.schemaId)
          ]),
          catchError((httpError: HttpErrorResponse) =>
            of(new DeleteFail(httpError))
          )
        )
    )
  );
}
