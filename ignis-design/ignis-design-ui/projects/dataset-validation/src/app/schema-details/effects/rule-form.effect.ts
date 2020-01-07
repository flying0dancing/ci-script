import { HttpErrorResponse } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Actions, Effect, ofType } from '@ngrx/effects';
import { of } from 'rxjs';
import { catchError, map, mergeMap, switchMap } from 'rxjs/operators';
import * as schemaActions from '../../schemas';
import { UpdateOne } from '../../schemas';
import { SchemasRepository } from '../../schemas/services/schemas.repository';
import {
  Delete,
  DeleteFail,
  DeleteSuccess,
  Post,
  PostFailed,
  PostSuccessful,
  RuleFormActionTypes
} from '../actions/rule-form.actions';
import { RulesRepository } from '../service/rules.repository';

@Injectable()
export class RuleFormEffects {
  @Effect()
  post$ = this.actions$.pipe(
    ofType(RuleFormActionTypes.Post),
    switchMap((postAction: Post) => {
      return this.schemaRepository
        .addRule(postAction.productId, postAction.schemaId, postAction.rule)
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
    ofType(RuleFormActionTypes.PostSuccessful),
    map(
      (postAction: PostSuccessful) =>
        new schemaActions.UpdateOne(postAction.productId, postAction.schemaId)
    )
  );

  @Effect()
  delete$ = this.actions$.pipe(
    ofType(RuleFormActionTypes.Delete),
    switchMap((event: Delete) =>
      this.rulesRepository
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

  constructor(
    private actions$: Actions,
    private schemaRepository: SchemasRepository,
    private rulesRepository: RulesRepository
  ) {}
}
