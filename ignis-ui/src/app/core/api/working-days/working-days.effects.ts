import { WorkingDaysService } from "@/core/api/working-days/working-days.service";
import { Injectable } from "@angular/core";
import { Actions, Effect, ofType } from "@ngrx/effects";
import { Action } from "@ngrx/store";
import { Observable, of } from "rxjs";
import { catchError, filter, map, switchMap, takeUntil } from "rxjs/operators";
import * as WorkingDaysActions from "./working-days.actions";

@Injectable()
export class WorkingDaysEffects {
  @Effect() getAllWorkingDays$: Observable<Action> = this.actions$.pipe(
    ofType<WorkingDaysActions.Get>(WorkingDaysActions.GET),
    switchMap(action => {
      const emptyWithMatchingKey$ = this.getEmptyWithMatchingKey$(action);

      return this.workingDaysService.getAll().pipe(
        map(
          workingDays =>
            new WorkingDaysActions.GetSuccess({
              reducerMapKey: action.payload.reducerMapKey,
              workingDays
            })
        ),
        catchError(() =>
          of(
            new WorkingDaysActions.GetFail({
              reducerMapKey: action.payload.reducerMapKey
            })
          )
        ),
        takeUntil(emptyWithMatchingKey$)
      );
    })
  );

  @Effect() update: Observable<Action> = this.actions$.pipe(
    ofType<WorkingDaysActions.Update>(WorkingDaysActions.UPDATE),
    switchMap(action => {
      const emptyWithMatchingKey$ = this.getEmptyWithMatchingKey$(action);

      return this.workingDaysService
        .update(action.payload.productId, action.payload.workingDays)
        .pipe(
          map(
            workingDays =>
              new WorkingDaysActions.UpdateSuccess({
                reducerMapKey: action.payload.reducerMapKey,
                newWorkingDays: workingDays,
                productName: action.payload.productName
              })
          ),
          catchError(() =>
            of(
              new WorkingDaysActions.UpdateFail({
                reducerMapKey: action.payload.reducerMapKey
              })
            )
          ),
          takeUntil(emptyWithMatchingKey$)
        );
    })
  );

  constructor(
    private workingDaysService: WorkingDaysService,
    private actions$: Actions
  ) {}

  private getEmptyWithMatchingKey$(action) {
    return this.actions$.pipe(
      ofType<WorkingDaysActions.Empty>(WorkingDaysActions.EMPTY),
      filter(
        emptyAction =>
          emptyAction.payload.reducerMapKey === action.payload.reducerMapKey
      )
    );
  }
}
