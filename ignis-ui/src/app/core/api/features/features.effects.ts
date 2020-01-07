import { FeaturesService } from "@/core/api/features/features.service";
import { Injectable } from "@angular/core";
import { Actions, Effect, ofType } from "@ngrx/effects";
import { Action } from "@ngrx/store";
import { Observable, of } from "rxjs";

import { catchError, filter, map, switchMap, takeUntil } from "rxjs/operators";

import * as FeaturesActions from "./features.actions";

@Injectable()
export class FeaturesEffects {
  @Effect() get$: Observable<Action> = this.actions$.pipe(
    ofType<FeaturesActions.Get>(FeaturesActions.GET),
    switchMap(action => {
      const emptyWithMatchingKey$ = this.actions$.pipe(
        ofType<FeaturesActions.Empty>(FeaturesActions.EMPTY),
        filter(
          emptyAction =>
            emptyAction.payload.reducerMapKey === action.payload.reducerMapKey
        )
      );

      return this.service.get().pipe(
        map(
          features =>
            new FeaturesActions.GetSuccess({
              reducerMapKey: action.payload.reducerMapKey,
              features
            })
        ),
        catchError(() =>
          of(
            new FeaturesActions.GetFail({
              reducerMapKey: action.payload.reducerMapKey
            })
          )
        ),
        takeUntil(emptyWithMatchingKey$)
      );
    })
  );

  constructor(private service: FeaturesService, private actions$: Actions) {}
}
