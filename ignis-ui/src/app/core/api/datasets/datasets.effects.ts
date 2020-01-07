import { Injectable } from "@angular/core";
import { Actions, Effect, ofType } from "@ngrx/effects";
import { Action, Store } from "@ngrx/store";
import { Observable, of } from "rxjs";
import { catchError, filter, map, switchMap, takeUntil } from "rxjs/operators";
import * as DatasetsActions from "./datasets.actions";
import { DatasetsService } from "./datasets.service";

@Injectable()
export class DatasetsEffects {
  @Effect() get$: Observable<Action> = this.actions$.pipe(
    ofType<DatasetsActions.Get>(DatasetsActions.GET),
    switchMap(action => {
      const emptyWithMatchingKey$ = this.getEmptyWithMatchingKey$(action);

      return this.service.get().pipe(
        map(res => (res._embedded ? res._embedded.datasetList : [])),
        map(
          datasets =>
            new DatasetsActions.GetSuccess({
              reducerMapKey: action.payload.reducerMapKey,
              datasets
            })
        ),
        catchError(() =>
          of(
            new DatasetsActions.GetFail({
              reducerMapKey: action.payload.reducerMapKey
            })
          )
        ),
        takeUntil(emptyWithMatchingKey$)
      );
    })
  );

  @Effect() getSilent$: Observable<Action> = this.actions$.pipe(
    ofType<DatasetsActions.GetSilent>(DatasetsActions.GET_SILENT),
    switchMap(action => {
      const emptyWithMatchingKey$ = this.getEmptyWithMatchingKey$(action);

      return this.service.get().pipe(
        map(res => res._embedded.datasetList),
        map(
          datasets =>
            new DatasetsActions.GetSilentSuccess({
              reducerMapKey: action.payload.reducerMapKey,
              datasets
            })
        ),
        catchError(() =>
          of(
            new DatasetsActions.GetFail({
              reducerMapKey: action.payload.reducerMapKey
            })
          )
        ),
        takeUntil(emptyWithMatchingKey$)
      );
    })
  );

  @Effect() getSourceFiles$: Observable<Action> = this.actions$.pipe(
    ofType<DatasetsActions.GetSourceFiles>(DatasetsActions.GET_SOURCE_FILES),
    switchMap(action => {
      const emptyWithMatchingKey$ = this.getEmptyWithMatchingKey$(action);

      return this.service.getSourceFiles().pipe(
        map(
          sourceFiles =>
            new DatasetsActions.GetSourceFilesSuccess({
              reducerMapKey: action.payload.reducerMapKey,
              sourceFiles
            })
        ),
        catchError(() =>
          of(
            new DatasetsActions.GetSourceFilesFail({
              reducerMapKey: action.payload.reducerMapKey
            })
          )
        ),
        takeUntil(emptyWithMatchingKey$)
      );
    })
  );

  constructor(
    private service: DatasetsService,
    private actions$: Actions,
    private store$: Store<any>
  ) {}

  private getEmptyWithMatchingKey$(action) {
    return this.actions$.pipe(
      ofType<DatasetsActions.Empty>(DatasetsActions.EMPTY),
      filter(
        emptyAction =>
          emptyAction.payload.reducerMapKey === action.payload.reducerMapKey
      )
    );
  }
}
