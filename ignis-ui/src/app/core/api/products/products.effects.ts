import { ProductsService } from '@/core/api/products/products.service';
import { Injectable } from '@angular/core';
import { Actions, Effect, ofType } from '@ngrx/effects';
import { Action } from '@ngrx/store';
import { Observable, of } from 'rxjs';

import { catchError, filter, map, switchMap, takeUntil } from 'rxjs/operators';
import { NAMESPACE as JOBS_NAMESPACE } from '../../../fcr/dashboard/jobs/jobs.constants';
import * as StagingActions from '../staging/staging.actions';

import * as ProductsActions from './products.actions';

@Injectable()
export class ProductsEffects {
  @Effect() get$: Observable<Action> = this.actions$.pipe(
    ofType<ProductsActions.Get>(ProductsActions.GET),
    switchMap(action => {
      const emptyWithMatchingKey$ = this.actions$.pipe(
        ofType<ProductsActions.Empty>(ProductsActions.EMPTY),
        filter(
          emptyAction =>
            emptyAction.payload.reducerMapKey === action.payload.reducerMapKey
        )
      );

      return this.service.get().pipe(
        map(
          products =>
            new ProductsActions.GetSuccess({
              reducerMapKey: action.payload.reducerMapKey,
              products
            })
        ),
        catchError(() =>
          of(
            new ProductsActions.GetFail({
              reducerMapKey: action.payload.reducerMapKey
            })
          )
        ),
        takeUntil(emptyWithMatchingKey$)
      );
    })
  );

  @Effect() importStarted$: Observable<Action> = this.actions$.pipe(
    ofType<ProductsActions.ImportStarted>(ProductsActions.IMPORT_STARTED),
    switchMap(action => {
      return of(new StagingActions.GetRunningJobs({ reducerMapKey: JOBS_NAMESPACE }));
    })
  );

  @Effect() delete$: Observable<Action> = this.actions$.pipe(
    ofType<ProductsActions.Delete>(ProductsActions.DELETE),
    switchMap(action => {
      return this.service.delete(action.payload.id).pipe(
        switchMap(id => [
          new ProductsActions.DeleteSuccess({
            reducerMapKey: action.payload.reducerMapKey,
            id
          }),
          new ProductsActions.Get({
            reducerMapKey: action.payload.reducerMapKey
          })
        ]),
        catchError(() =>
          of(
            new ProductsActions.DeleteFail({
              reducerMapKey: action.payload.reducerMapKey
            })
          )
        )
      );
    })
  );

  constructor(private service: ProductsService, private actions$: Actions) {}
}
