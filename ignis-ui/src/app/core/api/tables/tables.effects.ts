import { Injectable } from "@angular/core";
import { Actions, Effect, ofType } from "@ngrx/effects";
import { Action } from "@ngrx/store";
import { Observable, of } from "rxjs";
import {
  catchError,
  filter,
  map,
  mergeMap,
  switchMap,
  takeUntil
} from "rxjs/operators";
import * as TablesActions from "./tables.actions";

import { TablesService } from "./tables.service";

@Injectable()
export class TablesEffects {
  @Effect() getTables$: Observable<Action> = this.actions$.pipe(
    ofType<TablesActions.Get>(TablesActions.GET),
    switchMap(action => {
      const { reducerMapKey } = action.payload;

      return this.service.getTables().pipe(
        map(res => res.data),
        map(
          tables =>
            new TablesActions.GetSuccess({
              reducerMapKey: action.payload.reducerMapKey,
              tables
            })
        ),
        catchError(() =>
          of(
            new TablesActions.GetFail({
              reducerMapKey: action.payload.reducerMapKey
            })
          )
        ),
        takeUntil(this.empty$(reducerMapKey))
      );
    })
  );

  @Effect() delete$: Observable<Action> = this.actions$.pipe(
    ofType<TablesActions.Delete>(TablesActions.DELETE),
    switchMap(action => {
      const { id, reducerMapKey } = action.payload;
      return this.service.delete(action.payload.id).pipe(
        mergeMap(deleteSuccess => [
          new TablesActions.DeleteSuccess({
            reducerMapKey: action.payload.reducerMapKey
          }),
          new TablesActions.Get({ reducerMapKey: action.payload.reducerMapKey })
        ]),
        catchError(() =>
          of(
            new TablesActions.DeleteFail({
              reducerMapKey: action.payload.reducerMapKey
            })
          )
        ),
        takeUntil(this.empty$(reducerMapKey))
      );
    })
  );

  private empty$(reducerMapKey: string): Observable<Action> {
    return this.actions$.pipe(
      ofType<TablesActions.Empty>(TablesActions.EMPTY),
      filter(emptyAction => emptyAction.payload.reducerMapKey === reducerMapKey)
    );
  }

  constructor(private service: TablesService, private actions$: Actions) {}
}
